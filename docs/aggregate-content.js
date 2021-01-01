'use strict'

const camelCaseKeys = require('camelcase-keys')
const { createHash } = require('crypto')
const EventEmitter = require('events')
const expandPath = require('@antora/expand-path-helper')
const File = require('./file')
const flattenDeep = require('./flatten-deep')
const fs = require('fs-extra')
const getCacheDir = require('cache-directory')
const GitCredentialManagerStore = require('./git-credential-manager-store')
const git = require('isomorphic-git')
const invariably = { false: () => false, void: () => {} }
const { obj: map } = require('through2')
const matcher = require('matcher')
const mimeTypes = require('./mime-types-with-asciidoc')
const MultiProgress = require('multi-progress')
const ospath = require('path')
const { posix: path } = ospath
const posixify = ospath.sep === '\\' ? (p) => p.replace(/\\/g, '/') : undefined
const promiseFinally = require('./promise-finally')
const { fs: resolvePathGlobsFs, git: resolvePathGlobsGit } = require('./resolve-path-globs')
const vfs = require('vinyl-fs')
const yaml = require('js-yaml')

const {
  COMPONENT_DESC_FILENAME,
  CONTENT_CACHE_FOLDER,
  CONTENT_GLOB,
  FILE_MODES,
  GIT_CORE,
  GIT_OPERATION_LABEL_LENGTH,
  GIT_PROGRESS_PHASES,
  VALID_STATE_FILENAME,
} = require('./constants')

const ANY_SEPARATOR_RX = /[:/]/
const CSV_RX = /\s*,\s*/
const VENTILATED_CSV_RX = /\s*,\s+/
const EDIT_URL_TEMPLATE_VAR_RX = /\{(web_url|ref(?:hash|name)|path)\}/g
const GIT_SUFFIX_RX = /(?:(?:(?:\.git)?\/)?\.git|\/)$/
const GIT_URI_DETECTOR_RX = /:(?:\/\/|[^/\\])/
const HOSTED_GIT_REPO_RX = /^(?:https?:\/\/|.+@)(git(?:hub|lab)\.com|bitbucket\.org|pagure\.io)[/:](.+?)(?:\.git)?$/
const SHORTEN_REF_RX = /^refs\/(?:heads|remotes\/[^/]+|tags)\//
const SPACE_RX = / /g
const SUPERFLUOUS_SEPARATORS_RX = /^\/+|\/+$|\/+(?=\/)/g
const URL_AUTH_CLEANER_RX = /^(https?:\/\/)[^/@]*@/
const URL_AUTH_EXTRACTOR_RX = /^(https?:\/\/)(?:([^/:@]+)?(?::([^/@]+)?)?@)?(.*)/

/**
 * Aggregates files from the specified content sources so they can be loaded
 * into Antora's virtual file catalog.
 *
 * Currently assumes each source points to a local or remote git repository.
 * Clones the repository, if necessary, then walks the git tree (or worktree) of
 * the specified branches and tags, starting from the specified start path(s).
 * Creates a virtual file containing the contents and location metadata for each
 * file matched. The files are then roughly organized by component version.
 *
 * @memberof content-aggregator
 *
 * @param {Object} playbook - The configuration object for Antora.
 * @param {Object} playbook.dir - The working directory of the playbook.
 * @param {Object} playbook.runtime - The runtime configuration object for Antora.
 * @param {String} [playbook.runtime.cacheDir=undefined] - The base cache directory.
 * @param {Boolean} [playbook.runtime.fetch=undefined] - Whether to fetch
 * updates from managed git repositories.
 * @param {Boolean} [playbook.runtime.silent=false] - Whether to be silent
 * (suppresses progress bars and warnings).
 * @param {Boolean} [playbook.runtime.quiet=false] - Whether to be quiet
 * (suppresses progress bars).
 * @param {Array} playbook.git - The git configuration object for Antora.
 * @param {Boolean} [playbook.git.ensureGitSuffix=true] - Whether the .git
 * suffix is automatically appended to each repository URL, if missing.
 * @param {Array} playbook.content - An array of content sources.
 *
 * @returns {Promise<Object>} A map of files organized by component version.
 */
function aggregateContent (playbook) {
  const startDir = playbook.dir || '.'
  const { branches, editUrl, tags, sources } = playbook.content
  const sourcesByUrl = sources.reduce(
    (accum, source) => accum.set(source.url, [...(accum.get(source.url) || []), source]),
    new Map()
  )
  const { cacheDir, fetch, silent, quiet } = playbook.runtime
  const progress = !quiet && !silent && createProgress(sourcesByUrl.keys(), process.stdout)
  const { ensureGitSuffix, credentials } = Object.assign({ ensureGitSuffix: true }, playbook.git)
  const credentialManager = registerGitPlugins(credentials, startDir).get('credentialManager')
  return promiseFinally(
    ensureCacheDir(cacheDir, startDir).then((resolvedCacheDir) =>
      Promise.all(
        Array.from(sourcesByUrl, ([url, sources]) =>
          loadRepository(url, {
            cacheDir: resolvedCacheDir,
            credentialManager,
            fetchTags: tagsSpecified(sources, tags),
            progress,
            fetch,
            startDir,
            ensureGitSuffix,
          }).then(({ repo, authStatus }) =>
            Promise.all(
              sources.map((source) => {
                source = Object.assign({ branches, editUrl, tags }, source)
                // NOTE if repository is managed (has a url), we can assume the remote name is origin
                // TODO if the repo has no remotes, then remoteName should be undefined
                const remoteName = repo.url ? 'origin' : source.remote || 'origin'
                return collectFilesFromSource(source, repo, remoteName, authStatus)
              })
            )
          )
        )
      )
        .then(buildAggregate)
        .catch((err) => {
          progress && progress.terminate()
          throw err
        })
    ),
    unregisterGitPlugins
  )
}

function buildAggregate (componentVersionBuckets) {
  const aggregateMap = flattenDeep(componentVersionBuckets).reduce((accum, batch) => {
    const key = batch.version + '@' + batch.name
    const entry = accum.get(key)
    return accum.set(key, entry ? Object.assign(entry, batch, { files: [...entry.files, ...batch.files] }) : batch)
  }, new Map())
  return [...aggregateMap.values()]
}

async function loadRepository (url, opts) {
  let dir
  let repo
  let authStatus
  if (~url.indexOf(':') && GIT_URI_DETECTOR_RX.test(url)) {
    let displayUrl
    let credentials
    ;({ displayUrl, url, credentials } = extractCredentials(url))
    dir = ospath.join(opts.cacheDir, generateCloneFolderName(displayUrl))
    // NOTE the presence of the url property on the repo object implies the repository is remote
    repo = { core: GIT_CORE, dir, gitdir: dir, url, noGitSuffix: !opts.ensureGitSuffix, noCheckout: true }
    const credentialManager = opts.credentialManager
    const validStateFile = ospath.join(repo.gitdir, VALID_STATE_FILENAME)
    try {
      await fs.access(validStateFile)
      if (opts.fetch) {
        await fs.unlink(validStateFile)
        const fetchOpts = getFetchOptions(repo, opts.progress, displayUrl, credentials, opts.fetchTags, 'fetch')
        await git
          .fetch(fetchOpts)
          .then(() => {
            authStatus = credentials ? 'auth-embedded' : credentialManager.status({ url }) ? 'auth-required' : undefined
            return git.config(Object.assign({ path: 'remote.origin.private', value: authStatus }, repo))
          })
          .catch((fetchErr) => {
            fetchOpts.emitter && fetchOpts.emitter.emit('error', fetchErr)
            if (fetchErr.name === git.E.HTTPError && fetchErr.data.statusCode === 401) fetchErr.rethrow = true
            throw fetchErr
          })
          .then(() => fs.createFile(validStateFile).catch(invariably.void))
          .then(() => fetchOpts.emitter && fetchOpts.emitter.emit('complete'))
      } else {
        // NOTE use cached value from previous fetch
        authStatus = await git.config(Object.assign({ path: 'remote.origin.private' }, repo))
      }
    } catch (gitErr) {
      await fs.remove(dir)
      if (gitErr.rethrow) throw transformGitCloneError(gitErr, displayUrl)
      const fetchOpts = getFetchOptions(repo, opts.progress, displayUrl, credentials, opts.fetchTags, 'clone')
      await git
        .clone(fetchOpts)
        .then(() => git.resolveRef(Object.assign({ ref: 'HEAD', depth: 1 }, repo)))
        .then(() => {
          authStatus = credentials ? 'auth-embedded' : credentialManager.status({ url }) ? 'auth-required' : undefined
          return git.config(Object.assign({ path: 'remote.origin.private', value: authStatus }, repo))
        })
        .catch(async (cloneErr) => {
          await fs.remove(dir)
          // FIXME triggering the error handler here causes assertion problems in the test suite
          //fetchOpts.emitter && fetchOpts.emitter.emit('error', cloneErr)
          throw transformGitCloneError(cloneErr, displayUrl)
        })
        .then(() => fs.createFile(validStateFile).catch(invariably.void))
        .then(() => fetchOpts.emitter && fetchOpts.emitter.emit('complete'))
    }
  } else if (await isLocalDirectory((dir = expandPath(url, '~+', opts.startDir)))) {
    repo = (await isLocalDirectory(ospath.join(dir, '.git')))
      ? { core: GIT_CORE, dir }
      : { core: GIT_CORE, dir, gitdir: dir, noCheckout: true }
    await git.resolveRef(Object.assign({ ref: 'HEAD', depth: 1 }, repo)).catch(() => {
      throw new Error(
        `Local content source must be a git repository: ${dir}${url !== dir ? ' (url: ' + url + ')' : ''}`
      )
    })
  } else {
    throw new Error(`Local content source does not exist: ${dir}${url !== dir ? ' (url: ' + url + ')' : ''}`)
  }
  return { repo, authStatus }
}

function extractCredentials (url) {
  if ((url.startsWith('https://') || url.startsWith('http://')) && ~url.indexOf('@')) {
    // Common oauth2 formats: (QUESTION should we try to coerce token only into one of these formats?)
    // GitHub: <token>:x-oauth-basic@ (or <token>@)
    // GitHub App: x-access-token:<token>@
    // GitLab: oauth2:<token>@
    // BitBucket: x-token-auth:<token>@
    const [, scheme, username, password, rest] = url.match(URL_AUTH_EXTRACTOR_RX)
    const displayUrl = (url = scheme + rest)
    // NOTE if only username is present, assume it's an oauth token
    const credentials = username ? (password == null ? { token: username } : { username, password }) : {}
    return { displayUrl, url, credentials }
  } else if (url.startsWith('git@')) {
    return { displayUrl: url, url: 'https://' + url.substr(4).replace(':', '/') }
  } else {
    return { displayUrl: url, url }
  }
}

async function collectFilesFromSource (source, repo, remoteName, authStatus) {
  const originUrl = repo.url || (await resolveRemoteUrl(repo, remoteName))
  return selectReferences(source, repo, remoteName).then((refs) =>
    Promise.all(refs.map((ref) => collectFilesFromReference(source, repo, remoteName, authStatus, ref, originUrl)))
  )
}

// QUESTION should we resolve HEAD to a ref eagerly to avoid having to do a match on it?
async function selectReferences (source, repo, remote) {
  let { branches: branchPatterns, tags: tagPatterns } = source
  const isBare = repo.noCheckout
  const refs = new Map()

  if (tagPatterns) {
    tagPatterns = Array.isArray(tagPatterns)
      ? tagPatterns.map((pattern) => String(pattern))
      : String(tagPatterns).split(CSV_RX)
    if (tagPatterns.length) {
      const tags = await git.listTags(repo)
      for (const shortname of tags.length ? matcher(tags, tagPatterns) : tags) {
        // NOTE tags are stored using symbol keys to distinguish them from branches
        refs.set(Symbol(shortname), { shortname, fullname: 'tags/' + shortname, type: 'tag' })
      }
    }
  }

  if (branchPatterns) {
    const branchPatternsString = String(branchPatterns)
    if (branchPatternsString === 'HEAD' || branchPatternsString === '.') {
      // NOTE current branch is undefined when HEAD is detached
      const currentBranch = await getCurrentBranchName(repo, remote)
      if (currentBranch) {
        branchPatterns = [currentBranch]
      } else {
        if (!isBare) refs.set('HEAD', { shortname: 'HEAD', fullname: 'HEAD', type: 'branch', head: 'detached' })
        return Array.from(refs.values())
      }
    } else {
      branchPatterns = Array.isArray(branchPatterns)
        ? branchPatterns.map((pattern) => String(pattern))
        : branchPatternsString.split(CSV_RX)
      if (branchPatterns.length) {
        let headBranchIdx
        // NOTE we can assume at least two entries if HEAD or . are present
        if (~(headBranchIdx = branchPatterns.indexOf('HEAD')) || ~(headBranchIdx = branchPatterns.indexOf('.'))) {
          // NOTE current branch is undefined when HEAD is detached
          const currentBranch = await getCurrentBranchName(repo, remote)
          if (currentBranch) {
            // NOTE ignore if current branch is already in list
            if (~branchPatterns.indexOf(currentBranch)) {
              branchPatterns = branchPatterns.filter((_, idx) => idx !== headBranchIdx)
            } else {
              branchPatterns[headBranchIdx] = currentBranch
            }
          } else {
            if (!isBare) refs.set('HEAD', { shortname: 'HEAD', fullname: 'HEAD', type: 'branch', head: 'detached' })
            branchPatterns = branchPatterns.filter((_, idx) => idx !== headBranchIdx)
          }
        }
      } else {
        return Array.from(refs.values())
      }
    }
    let remoteBranches = await git.listBranches(Object.assign({ remote }, repo))
    if (remoteBranches.length) {
      // NOTE isomorphic-git includes HEAD in list of remote branches (see https://isomorphic-git.org/docs/listBranches)
      const headIdx = remoteBranches.indexOf('HEAD')
      if (~headIdx) remoteBranches = remoteBranches.filter((_, idx) => idx !== headIdx)
      for (const shortname of remoteBranches.length ? matcher(remoteBranches, branchPatterns) : remoteBranches) {
        refs.set(shortname, { shortname, fullname: path.join('remotes', remote, shortname), type: 'branch', remote })
      }
    }
    // NOTE only consider local branches if repo has a worktree or there are no remote tracking branches
    if (!isBare) {
      const localBranches = await git.listBranches(repo)
      if (localBranches.length) {
        const currentBranch = await git.currentBranch(repo)
        for (const shortname of matcher(localBranches, branchPatterns)) {
          const ref = { shortname, fullname: 'heads/' + shortname, type: 'branch' }
          if (shortname === currentBranch) ref.head = true
          refs.set(shortname, ref)
        }
      }
    } else if (!remoteBranches.length) {
      // QUESTION should local branches be used if only remote branch is HEAD?
      const localBranches = await git.listBranches(repo)
      for (const shortname of localBranches.length ? matcher(localBranches, branchPatterns) : localBranches) {
        refs.set(shortname, { shortname, fullname: 'heads/' + shortname, type: 'branch' })
      }
    }
  }

  return Array.from(refs.values())
}

/**
 * Returns the current branch name unless the HEAD is detatched.
 */
function getCurrentBranchName (repo, remote) {
  let refPromise
  if (repo.noCheckout) {
    refPromise = git
      .resolveRef(Object.assign({ ref: 'refs/remotes/' + remote + '/HEAD', depth: 2 }, repo))
      .catch(() => git.resolveRef(Object.assign({ ref: 'HEAD', depth: 2 }, repo)))
  } else {
    refPromise = git.resolveRef(Object.assign({ ref: 'HEAD', depth: 2 }, repo))
  }
  return refPromise.then((ref) => (ref.startsWith('refs/') ? ref.replace(SHORTEN_REF_RX, '') : undefined))
}

async function collectFilesFromReference (source, repo, remoteName, authStatus, ref, originUrl) {
  const url = repo.url
  const displayUrl = url || repo.dir
  const editUrl = source.editUrl
  let worktreePath
  if (ref.head && !(url || repo.noCheckout)) {
    worktreePath = repo.dir
  } else {
    ref.oid = await git.resolveRef(Object.assign({ ref: 'refs/' + ref.fullname }, repo))
  }
  if ('startPaths' in source) {
    let startPaths
    startPaths = Array.isArray((startPaths = source.startPaths))
      ? startPaths.map(coerceToString).map(cleanStartPath)
      : (startPaths = coerceToString(startPaths)) && startPaths.split(VENTILATED_CSV_RX).map(cleanStartPath)
    startPaths = await (worktreePath
      ? resolvePathGlobsFs(worktreePath, startPaths)
      : resolvePathGlobsGit(repo, ref.oid, startPaths))
    if (!startPaths.length) {
      const refInfo = `ref: ${ref.fullname.replace(/^heads\//, '')}${worktreePath ? ' <worktree>' : ''}`
      throw new Error(`no start paths found in ${displayUrl} (${refInfo})`)
    }
    return Promise.all(
      startPaths.map((startPath) =>
        collectFilesFromStartPath(startPath, repo, authStatus, ref, worktreePath, originUrl, editUrl)
      )
    )
  }
  const startPath = cleanStartPath(coerceToString(source.startPath))
  return collectFilesFromStartPath(startPath, repo, authStatus, ref, worktreePath, originUrl, editUrl)
}

function collectFilesFromStartPath (startPath, repo, authStatus, ref, worktreePath, originUrl, editUrl) {
  return (worktreePath
    ? readFilesFromWorktree(worktreePath, startPath)
    : readFilesFromGitTree(repo, ref.oid, startPath)
  )
    .then((files) => {
      const componentVersionBucket = loadComponentDescriptor(files, ref)
      const origin = computeOrigin(originUrl, authStatus, ref, startPath, worktreePath, editUrl)
      componentVersionBucket.files = files.map((file) => assignFileProperties(file, origin))
      return componentVersionBucket
    })
    .catch((err) => {
      const refInfo = `ref: ${ref.fullname.replace(/^heads\//, '')}${worktreePath ? ' <worktree>' : ''}`
      const pathInfo = !startPath || err.message.startsWith('the start path ') ? '' : ' | path: ' + startPath
      err.message += ` in ${repo.url || repo.dir} (${refInfo}${pathInfo})`
      throw err
    })
}

function readFilesFromWorktree (worktreePath, startPath) {
  const cwd = path.join(worktreePath, startPath)
  return fs
    .stat(cwd)
    .catch(() => {
      throw new Error(`the start path '${startPath}' does not exist`)
    })
    .then((stat) => {
      if (!stat.isDirectory()) throw new Error(`the start path '${startPath}' is not a directory`)
      return new Promise((resolve, reject) =>
        vfs
          .src(CONTENT_GLOB, { cwd, removeBOM: false })
          .on('error', reject)
          .pipe(relativizeFiles())
          .pipe(collectFiles(resolve))
      )
    })
}

/**
 * Transforms the path of every file in the stream to a relative posix path.
 *
 * Applies a mapping function to all files in the stream so they end up with a
 * posixified path relative to the file's base instead of the filesystem root.
 * This mapper also filters out any directories (indicated by file.isNull())
 * that got caught up in the glob.
 */
function relativizeFiles () {
  return map((file, enc, next) => {
    if (file.isNull()) {
      next()
    } else {
      next(
        null,
        new File({
          path: posixify ? posixify(file.relative) : file.relative,
          contents: file.contents,
          stat: file.stat,
          src: { abspath: file.path },
        })
      )
    }
  })
}

function collectFiles (done) {
  const accum = []
  return map((file, enc, next) => accum.push(file) && next(), () => done(accum)) // prettier-ignore
}

function readFilesFromGitTree (repo, oid, startPath) {
  return getGitTree(repo, oid, startPath).then((tree) => srcGitTree(repo, tree))
}

function getGitTree (repo, oid, startPath) {
  return git
    .readTree(Object.assign({ oid, filepath: startPath }, repo))
    .catch(({ code }) => {
      throw new Error(
        `the start path '${startPath}' ${code === git.E.ResolveTreeError ? 'is not a directory' : 'does not exist'}`
      )
    })
    .then(({ tree }) => tree)
}

function srcGitTree (repo, tree) {
  return new Promise((resolve, reject) => {
    const files = []
    walkGitTree(repo, tree, filterGitEntry)
      .on('entry', (entry) => files.push(entryToFile(entry)))
      .on('error', reject)
      .on('end', () => resolve(Promise.all(files)))
      .start()
  })
}

function walkGitTree (repo, root, filter) {
  const emitter = new EventEmitter()
  let depth = 1
  function visit (tree, dirname = '') {
    depth--
    for (const entry of tree) {
      if (filter(entry)) {
        const type = entry.type
        if (type === 'blob') {
          const mode = FILE_MODES[entry.mode]
          if (mode) {
            emitter.emit(
              'entry',
              Object.assign({}, repo, { mode, oid: entry.oid, path: path.join(dirname, entry.path) })
            )
          }
        } else if (type === 'tree') {
          depth++
          git
            .readTree(Object.assign({ oid: entry.oid }, repo))
            .then(({ tree: subtree }) => visit(subtree, path.join(dirname, entry.path)))
            .catch((err) => emitter.emit('error', err))
        }
      }
    }
    if (depth === 0) emitter.emit('end')
  }
  emitter.start = () => visit(root)
  return emitter
}

/**
 * Returns true if the entry should be processed or false if it should be skipped.
 * Ignores files that begin with dot ('.') (entry.path is a basename) or that do
 * not have a file extension.
 */
function filterGitEntry (entry) {
  return entry.path.charAt() !== '.' && (entry.type !== 'blob' || ~entry.path.indexOf('.'))
}

function entryToFile (entry) {
  return git.readBlob(entry).then(({ blob: contents }) => {
    const stat = new fs.Stats()
    stat.mode = entry.mode
    stat.mtime = undefined
    stat.size = contents.length
    return new File({ path: entry.path, contents, stat })
  })
}

function loadComponentDescriptor (files, ref) {
  const descriptorFileIdx = files.findIndex((file) => file.path === COMPONENT_DESC_FILENAME)
  if (descriptorFileIdx < 0) throw new Error(`${COMPONENT_DESC_FILENAME} not found`)
  const descriptorFile = files[descriptorFileIdx]
  files.splice(descriptorFileIdx, 1)
  let data
  try {
    data = yaml.safeLoad(descriptorFile.contents.toString())
  } catch (e) {
    e.message = `${COMPONENT_DESC_FILENAME} has invalid syntax; ${e.message}`
    throw e
  }
  if (data.name == null) throw new Error(`${COMPONENT_DESC_FILENAME} is missing a name`)
  const name = String(data.name)
  if (name === '.' || name === '..' || ~name.indexOf('/')) {
    throw new Error(`name in ${COMPONENT_DESC_FILENAME} cannot have path segments: ${name}`)
  }
  if (data.use_ref) {
    data.version = ref.shortname
    data.display_version = ref.shortname
  }
  if (data.version == null) throw new Error(`${COMPONENT_DESC_FILENAME} is missing a version`)
  const version = String(data.version)
  if (version === '.' || version === '..' || ~version.indexOf('/')) {
    throw new Error(`version in ${COMPONENT_DESC_FILENAME} cannot have path segments: ${version}`)
  }
  data.name = name
  data.version = version
  return camelCaseKeys(data, { deep: true, stopPaths: ['asciidoc'] })
}

function computeOrigin (url, authStatus, ref, startPath, worktreePath = undefined, editUrl = true) {
  const { shortname: refname, oid: refhash, type: reftype } = ref
  const origin = { type: 'git', startPath }
  if (url) origin.url = url
  if (authStatus) origin.private = authStatus
  origin[reftype] = refname
  if (worktreePath) {
    origin.fileUriPattern =
      'file://' + (posixify ? '/' + posixify(worktreePath) : worktreePath) + path.join('/', startPath, '%s')
    // Q: should we set worktreePath instead (or additionally?)
    origin.worktree = true
  } else {
    origin.refhash = refhash
  }
  if (editUrl === true) {
    let match
    if (url && (match = url.match(HOSTED_GIT_REPO_RX))) {
      const host = match[1]
      let action
      let category = ''
      if (host === 'pagure.io') {
        action = 'blob'
        category = 'f'
      } else if (host === 'bitbucket.org') {
        action = 'src'
      } else {
        action = reftype === 'branch' ? 'edit' : 'blob'
      }
      origin.editUrlPattern = 'https://' + path.join(match[1], match[2], action, refname, category, startPath, '%s')
    }
  } else if (editUrl) {
    const vars = {
      path: () => (startPath ? path.join(startPath, '%s') : '%s'),
      refhash: () => refhash,
      refname: () => refname,
      web_url: () => (url ? url.replace(GIT_SUFFIX_RX, '') : ''),
    }
    origin.editUrlPattern = editUrl.replace(EDIT_URL_TEMPLATE_VAR_RX, (_, name) => vars[name]())
  }
  return origin
}

function assignFileProperties (file, origin) {
  const extname = file.extname
  if (!file.src) file.src = {}
  Object.assign(file.src, { path: file.path, basename: file.basename, stem: file.stem, extname, origin })
  file.mediaType = file.src.mediaType = mimeTypes.lookup(extname)
  if (origin.fileUriPattern) {
    const fileUri = origin.fileUriPattern.replace('%s', file.src.path)
    file.src.fileUri = ~fileUri.indexOf(' ') ? fileUri.replace(SPACE_RX, '%20') : fileUri
  }
  if (origin.editUrlPattern) {
    const editUrl = origin.editUrlPattern.replace('%s', file.src.path)
    file.src.editUrl = ~editUrl.indexOf(' ') ? editUrl.replace(SPACE_RX, '%20') : editUrl
  }
  return file
}

function getFetchOptions (repo, progress, url, credentials, fetchTags, operation) {
  const opts = Object.assign({ depth: 1 }, credentials, repo)
  if (progress) opts.emitter = createProgressEmitter(progress, url, operation)
  if (operation === 'fetch') {
    opts.prune = true
    if (fetchTags) opts.tags = opts.pruneTags = true
  } else if (!fetchTags) {
    opts.noTags = true
  }
  return opts
}

function createProgress (urls, term) {
  if (term.isTTY && term.columns > 59) {
    //term.write('Aggregating content...\n')
    let maxUrlLength = 0
    for (const url of urls) {
      if (~url.indexOf(':') && GIT_URI_DETECTOR_RX.test(url)) {
        const urlLength = extractCredentials(url).displayUrl.length
        if (urlLength > maxUrlLength) maxUrlLength = urlLength
      }
    }
    const progress = new MultiProgress(term)
    // NOTE remove the width of the operation, then partition the difference between the url and bar
    progress.maxLabelWidth = Math.min(Math.ceil((term.columns - GIT_OPERATION_LABEL_LENGTH) / 2), maxUrlLength)
    return progress
  }
}

function createProgressEmitter (progress, progressLabel, operation) {
  const progressBar = progress.newBar(formatProgressBar(progressLabel, progress.maxLabelWidth, operation), {
    total: 100,
    complete: '#',
    incomplete: '-',
  })
  const ticks = progressBar.stream.columns - progressBar.fmt.replace(':bar', '').length
  // NOTE leave room for indeterminate progress at end of bar; this isn't strictly needed for a bare clone
  progressBar.scaleFactor = Math.max(0, (ticks - 1) / ticks)
  progressBar.tick(0)
  return new EventEmitter()
    .on('progress', onGitProgress.bind(null, progressBar))
    .on('complete', onGitComplete.bind(null, progressBar))
    .on('error', onGitComplete.bind(null, progressBar))
}

function formatProgressBar (label, maxLabelWidth, operation) {
  const paddingSize = maxLabelWidth - label.length
  let padding = ''
  if (paddingSize < 0) {
    label = '...' + label.substr(-paddingSize + 3)
  } else if (paddingSize) {
    padding = ' '.repeat(paddingSize)
  }
  // NOTE assume operation has a fixed length
  return `[${operation}] ${label}${padding} [:bar]`
}

function onGitProgress (progressBar, { phase, loaded, total }) {
  const phaseIdx = GIT_PROGRESS_PHASES.indexOf(phase)
  if (~phaseIdx) {
    const scaleFactor = progressBar.scaleFactor
    let ratio = ((loaded / total) * scaleFactor) / GIT_PROGRESS_PHASES.length
    if (phaseIdx) ratio += (phaseIdx * scaleFactor) / GIT_PROGRESS_PHASES.length
    // TODO if we upgrade to progress >= 2.0.0, UI updates are automatically throttled (set via renderThrottle option)
    //setTimeout(() => progressBar.update(ratio > scaleFactor ? scaleFactor : ratio), 0)
    progressBar.update(ratio > scaleFactor ? scaleFactor : ratio)
  }
}

function onGitComplete (progressBar, err) {
  if (err) {
    progressBar.chars.incomplete = '?'
    progressBar.update(0)
  } else {
    progressBar.update(1)
  }
}

/**
 * Generates a safe, unique folder name for a git URL.
 *
 * The purpose of this function is generate a safe, unique folder name for the cloned
 * repository that gets stored in the cache directory.
 *
 * The generated folder name follows the pattern: <basename>-<sha1>-<version>.git
 *
 * @param {String} url - The repository URL to convert.
 * @returns {String} The generated folder name.
 */
function generateCloneFolderName (url) {
  let normalizedUrl = url.toLowerCase()
  if (posixify) normalizedUrl = posixify(normalizedUrl)
  normalizedUrl = normalizedUrl.replace(GIT_SUFFIX_RX, '')
  const basename = normalizedUrl.split(ANY_SEPARATOR_RX).pop()
  const hash = createHash('sha1')
  hash.update(normalizedUrl)
  return basename + '-' + hash.digest('hex') + '.git'
}

/**
 * Resolve the HTTP URL of the specified remote for the given repository, removing embedded auth if present.
 *
 * @param {Repository} repo - The repository on which to operate.
 * @param {String} remoteName - The name of the remote to resolve.
 * @returns {String} The URL of the specified remote, if present.
 */
async function resolveRemoteUrl (repo, remoteName) {
  return git.config(Object.assign({ path: 'remote.' + remoteName + '.url' }, repo)).then((url) => {
    if (!url) return
    if (url.startsWith('https://') || url.startsWith('http://')) {
      return ~url.indexOf('@') ? url.replace(URL_AUTH_CLEANER_RX, '$1') : url
    } else if (url.startsWith('git@')) {
      return 'https://' + url.substr(4).replace(':', '/')
    }
  })
}

/**
 * Checks whether the specified URL matches a directory on the local filesystem.
 *
 * @param {String} url - The URL to check.
 * @return {Boolean} A flag indicating whether the URL matches a directory on the local filesystem.
 */
function isLocalDirectory (url) {
  return fs
    .stat(url)
    .then((stat) => stat.isDirectory())
    .catch(invariably.false)
}

function tagsSpecified (sources, defaultTags) {
  return ~sources.findIndex((source) => {
    const tags = source.tags || defaultTags || []
    return Array.isArray(tags) ? tags.length : true
  })
}

function registerGitPlugins (config, startDir) {
  const plugins = git.cores.create(GIT_CORE)
  if (!plugins.has('fs')) plugins.set('fs', Object.assign({ _managed: true }, fs))
  let credentialManager
  if (plugins.has('credentialManager')) {
    credentialManager = plugins.get('credentialManager')
    if (typeof credentialManager.configure === 'function') credentialManager.configure({ config, startDir })
    if (typeof credentialManager.status !== 'function') {
      plugins.set('credentialManager', Object.assign({}, credentialManager, { status () {} }))
    }
  } else {
    ;(credentialManager = new GitCredentialManagerStore().configure({ config, startDir }))._managed = true
    plugins.set('credentialManager', credentialManager)
  }
  return plugins
}

function unregisterGitPlugins () {
  git.cores.create(GIT_CORE).forEach((val, key, map) => val._managed && map.delete(key))
}

/**
 * Expands the content cache directory path and ensures it exists.
 *
 * @param {String} preferredCacheDir - The preferred cache directory. If the value is undefined,
 *   the user's cache folder is used.
 * @param {String} startDir - The directory to use in place of a leading '.' segment.
 *
 * @returns {Promise<String>} A promise that resolves to the absolute content cache directory.
 */
function ensureCacheDir (preferredCacheDir, startDir) {
  // QUESTION should fallback directory be relative to cwd, playbook dir, or tmpdir?
  const baseCacheDir =
    preferredCacheDir == null
      ? getCacheDir('antora' + (process.env.NODE_ENV === 'test' ? '-test' : '')) || ospath.resolve('.antora/cache')
      : expandPath(preferredCacheDir, '~+', startDir)
  const cacheDir = ospath.join(baseCacheDir, CONTENT_CACHE_FOLDER)
  return fs.ensureDir(cacheDir).then(() => cacheDir)
}

function transformGitCloneError (err, displayUrl) {
  let msg
  const { code, data, message } = err
  if (code === git.E.HTTPError) {
    if (data.statusCode === 401) {
      if (err.rejected) {
        msg = 'Content repository not found or credentials were rejected'
      } else {
        msg = 'Content repository not found or requires credentials'
      }
    } else if (data.statusCode === 404) {
      msg = 'Content repository not found'
    } else {
      msg = message.trimRight()
    }
  } else if (code === git.E.RemoteUrlParseError || code === git.E.UnknownTransportError) {
    msg = 'Content source uses an unsupported transport protocol'
  } else if (code && data) {
    msg = (~message.indexOf('. ') ? message : message.replace(/\.$/, '')).trimRight()
  } else {
    msg = 'Unknown ' + err.name + ': See cause'
  }
  const wrappedErr = new Error(msg + ' (url: ' + displayUrl + ')')
  wrappedErr.stack += '\nCaused by: ' + (err.stack || 'unknown')
  return wrappedErr
}

function coerceToString (value) {
  return value == null ? '' : String(value)
}

function cleanStartPath (value) {
  return value && ~value.indexOf('/') ? value.replace(SUPERFLUOUS_SEPARATORS_RX, '') : value
}

module.exports = aggregateContent
module.exports._computeOrigin = computeOrigin
