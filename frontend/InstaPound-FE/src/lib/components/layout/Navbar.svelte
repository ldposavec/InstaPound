<script lang="ts">
	import { Camera, Menu, X, User, LogOut, Settings, Upload, Search, Crown, Shield } from 'lucide-svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import Button from '$lib/components/ui/Button.svelte';

	let mobileMenuOpen = $state(false);

	const navLinks = [
		{ href: '/', label: 'Browse', icon: Camera },
		{ href: '/search', label: 'Search', icon: Search }
	];

	const authLinks = [{ href: '/upload', label: 'Upload', icon: Upload }];

	async function handleLogout() {
		await authStore.logout();
		window.location.href = '/';
	}
</script>

<nav class="sticky top-0 z-40 border-b border-slate-200/80 bg-white/80 backdrop-blur-xl dark:border-slate-800/80 dark:bg-slate-950/80">
	<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
		<div class="flex h-16 items-center justify-between">
			<!-- Logo -->
			<a href="/" class="flex items-center gap-2">
				<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-indigo-600 shadow-lg shadow-violet-500/25">
					<Camera class="h-5 w-5 text-white" />
				</div>
				<span class="text-xl font-bold bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
					InstaPound
				</span>
			</a>

			<!-- Desktop Nav -->
			<div class="hidden items-center gap-1 md:flex">
				{#each navLinks as link}
					<a
						href={link.href}
						class="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100"
					>
						<svelte:component this={link.icon} class="h-4 w-4" />
						{link.label}
					</a>
				{/each}
				{#if authStore.isAuthenticated}
					{#each authLinks as link}
						<a
							href={link.href}
							class="flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100"
						>
							<svelte:component this={link.icon} class="h-4 w-4" />
							{link.label}
						</a>
					{/each}
				{/if}
			</div>

			<!-- Desktop Auth -->
			<div class="hidden items-center gap-3 md:flex">
				{#if authStore.loading}
					<div class="h-8 w-20 animate-pulse rounded-lg bg-slate-200 dark:bg-slate-800"></div>
				{:else if authStore.isAuthenticated && authStore.user}
					<a
						href="/pricing"
						class="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-amber-600 transition-colors hover:bg-amber-50 dark:text-amber-400 dark:hover:bg-amber-950/50"
					>
						<Crown class="h-4 w-4" />
						{authStore.user.packageType}
					</a>
					{#if authStore.isAdmin}
						<a
							href="/admin"
							class="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/50"
						>
							<Shield class="h-4 w-4" />
							Admin
						</a>
					{/if}
					<div class="relative">
						<a
							href="/profile"
							class="flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
						>
							<div class="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-xs font-semibold text-white">
								{authStore.user.username.charAt(0).toUpperCase()}
							</div>
							{authStore.user.username}
						</a>
					</div>
					<button
						onclick={handleLogout}
						class="flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100"
					>
						<LogOut class="h-4 w-4" />
					</button>
				{:else}
					<a href="/login">
						<Button variant="ghost" size="sm">Sign In</Button>
					</a>
					<a href="/register">
						<Button size="sm">Get Started</Button>
					</a>
				{/if}
			</div>

			<!-- Mobile menu button -->
			<button
				onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
				class="rounded-lg p-2 text-slate-600 transition-colors hover:bg-slate-100 md:hidden dark:text-slate-400 dark:hover:bg-slate-800"
			>
				{#if mobileMenuOpen}
					<X class="h-6 w-6" />
				{:else}
					<Menu class="h-6 w-6" />
				{/if}
			</button>
		</div>
	</div>

	<!-- Mobile menu -->
	{#if mobileMenuOpen}
		<div class="border-t border-slate-200 bg-white md:hidden dark:border-slate-800 dark:bg-slate-950">
			<div class="space-y-1 px-4 py-3">
				{#each navLinks as link}
					<a
						href={link.href}
						onclick={() => (mobileMenuOpen = false)}
						class="flex items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-slate-700 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
					>
						<svelte:component this={link.icon} class="h-5 w-5" />
						{link.label}
					</a>
				{/each}
				{#if authStore.isAuthenticated}
					{#each authLinks as link}
						<a
							href={link.href}
							onclick={() => (mobileMenuOpen = false)}
							class="flex items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-slate-700 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
						>
							<svelte:component this={link.icon} class="h-5 w-5" />
							{link.label}
						</a>
					{/each}
					<a
						href="/profile"
						onclick={() => (mobileMenuOpen = false)}
						class="flex items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-slate-700 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
					>
						<User class="h-5 w-5" />
						Profile
					</a>
					<a
						href="/pricing"
						onclick={() => (mobileMenuOpen = false)}
						class="flex items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-amber-600 transition-colors hover:bg-amber-50 dark:text-amber-400 dark:hover:bg-amber-950/50"
					>
						<Crown class="h-5 w-5" />
						{authStore.user?.packageType} Package
					</a>
					{#if authStore.isAdmin}
						<a
							href="/admin"
							onclick={() => (mobileMenuOpen = false)}
							class="flex items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/50"
						>
							<Shield class="h-5 w-5" />
							Admin Dashboard
						</a>
					{/if}
					<button
						onclick={() => {
							mobileMenuOpen = false;
							handleLogout();
						}}
						class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-base font-medium text-slate-700 transition-colors hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
					>
						<LogOut class="h-5 w-5" />
						Sign Out
					</button>
				{:else}
					<div class="flex gap-3 pt-3">
						<a href="/login" class="flex-1" onclick={() => (mobileMenuOpen = false)}>
							<Button variant="outline" class="w-full">Sign In</Button>
						</a>
						<a href="/register" class="flex-1" onclick={() => (mobileMenuOpen = false)}>
							<Button class="w-full">Get Started</Button>
						</a>
					</div>
				{/if}
			</div>
		</div>
	{/if}
</nav>
