<script lang="ts">
	import {
		Content,
		Grid,
		Header,
		HeaderAction,
		HeaderPanelDivider,
		HeaderPanelLink,
		HeaderPanelLinks,
		HeaderUtilities,
		SideNav,
		SideNavDivider,
		SideNavItems,
		SideNavLink,
		SkipToContent,
	} from "carbon-components-svelte"
	import "./styles.css"
	import type { CarbonIcon } from "carbon-icons-svelte"
	import { Login, Logout, Notebook, UserAvatar, UserAvatarFilledAlt } from "carbon-icons-svelte"
	import { user } from "$lib/stores/user"
	import { page } from "$app/stores"
	import { _blueprintsRouteId } from "./blueprints/route"
	import { _logoutRouteId } from "./logout/route"
	import { _loginRouteId } from "./login/route"
	import { base } from "$app/paths"
	import { goto } from "$app/navigation"
	import Alerts from "../lib/components/Alerts.svelte"

	let isSideNavOpen = false
	let userIcon: CarbonIcon
	$: {
		if ($user.loggedIn) {
			userIcon = UserAvatarFilledAlt
		} else {
			userIcon = UserAvatar
			goto(_loginRouteId)
		}
	}
</script>

<svelte:head>
	<title>Clustercode</title>
	<meta name="description" content="Clustercode Web UI" />
</svelte:head>

<Header company="Clustercode" platformName="UI" bind:isSideNavOpen href={base}>
	<svelte:fragment slot="skip-to-content">
		<SkipToContent />
	</svelte:fragment>
	<HeaderUtilities>
		<HeaderAction icon={userIcon} closeIcon={userIcon}>
			<HeaderPanelLinks>
				{#if $user.loggedIn}
					<HeaderPanelDivider>{$user.fullName}</HeaderPanelDivider>
					<HeaderPanelLink href={_logoutRouteId}>
						<Logout />
						Logout
					</HeaderPanelLink>
				{:else}
					<HeaderPanelLink href={_loginRouteId}>
						<Login />
						Login
					</HeaderPanelLink>
				{/if}
				<HeaderPanelDivider />
			</HeaderPanelLinks>
		</HeaderAction>
	</HeaderUtilities>
</Header>

{#if $user.loggedIn}
	<SideNav bind:isOpen={isSideNavOpen} rail>
		<SideNavItems>
			<SideNavLink
				icon={Notebook}
				text="Blueprints"
				href={_blueprintsRouteId}
				isSelected={$page.route.id === _blueprintsRouteId}
			/>
			<SideNavDivider />
		</SideNavItems>
	</SideNav>
{/if}

<Content>
	<Alerts />
	<Grid padding>
		<slot />
	</Grid>
</Content>
