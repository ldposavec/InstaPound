<script lang="ts">
    import { Camera } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import { goto } from '$app/navigation';

    let username = $state('');
    let password = $state('');
    let loading = $state(false);
    let error = $state('');

    async function handleSubmit(e: Event) {
        e.preventDefault();
        loading = true;
        error = '';
        try {
            await authStore.login(username, password);
            goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed. Please try again.';
        } finally {
            loading = false;
        }
    }

    function handleOAuth(provider: 'google' | 'github' | 'okta') {
        window.location.href = `/api/oauth2/authorization/${provider}`;
    }
</script>

<svelte:head>
    <title>Sign In - InstaPound</title>
</svelte:head>

<div class="flex min-h-[calc(100vh-200px)] items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
        <div class="mb-8 text-center">
            <a href="/" class="inline-flex items-center gap-2">
                <div class="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-indigo-600 shadow-lg shadow-violet-500/25">
                    <Camera class="h-6 w-6 text-white" />
                </div>
                <span class="text-2xl font-bold bg-gradient-to-r from-violet-600 to-indigo-600 bg-clip-text text-transparent">
					InstaPound
				</span>
            </a>
        </div>
        <Card variant="elevated" padding="lg">
            <div class="mb-6 text-center">
                <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Welcome back</h1>
                <p class="mt-2 text-slate-600 dark:text-slate-400">Sign in to your account to continue</p>
            </div>
            {#if error}
                <Alert variant="error" class="mb-6" dismissible>
                    {error}
                </Alert>
            {/if}
            <form onsubmit={handleSubmit} class="space-y-5">
                <Input
                        type="text"
                        label="Username"
                        placeholder="Enter your username"
                        bind:value={username}
                        required
                />
                <Input
                        type="password"
                        label="Password"
                        placeholder="Enter your password"
                        bind:value={password}
                        required
                />
                <Button type="submit" class="w-full" {loading}>
                    Sign In
                </Button>
            </form>
            <div class="relative my-6">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-slate-200 dark:border-slate-700"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="bg-white px-4 text-slate-500 dark:bg-slate-900 dark:text-slate-400">Or continue with</span>
                </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
                <button
                        onclick={() => handleOAuth('okta')}
                        class="flex items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
                >
                    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 0C5.389 0 0 5.35 0 12s5.35 12 12 12 12-5.35 12-12S18.611 0 12 0zm0 18c-3.325 0-6-2.675-6-6s2.675-6 6-6 6 2.675 6 6-2.675 6-6 6z"/>
                    </svg>
                    Okta
                </button>
            </div>
            <p class="mt-6 text-center text-sm text-slate-600 dark:text-slate-400">
                Don't have an account?
                <a href="/register" class="font-medium text-violet-600 hover:text-violet-700 dark:text-violet-400 dark:hover:text-violet-300">
                    Create one now
                </a>
            </p>
        </Card>
    </div>
</div>