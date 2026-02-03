<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import Navbar from '$lib/components/layout/Navbar.svelte';
	import Footer from '$lib/components/layout/Footer.svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import { onMount } from 'svelte';

	let { children } = $props();

	onMount(() => {
		authStore.checkAuth();
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>InstaPound - Share Your Photos</title>
</svelte:head>

<div class="flex min-h-screen flex-col bg-slate-50 dark:bg-slate-950">
	<Navbar />
	<main class="flex-1">
		{@render children()}
	</main>
	<Footer />
</div>
