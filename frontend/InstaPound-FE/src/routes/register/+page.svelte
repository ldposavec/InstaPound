<script lang="ts">
    import { Camera, Check } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import { authApi } from '$lib/api';
    import { goto } from '$app/navigation';
    import type { PackageType } from '$lib/types';

    let username = $state('');
    let email = $state('');
    let password = $state('');
    let confirmPassword = $state('');
    let selectedPackage = $state<PackageType>('FREE');
    let loading = $state(false);
    let error = $state('');
    let usernameError = $state('');
    let emailError = $state('');
    let passwordError = $state('');

    const packages = [
        {
            type: 'FREE' as PackageType,
            name: 'Free',
            price: '$0',
            description: 'Perfect for getting started',
            features: ['5 uploads per day', '10MB max file size', '50 total photos']
        },
        {
            type: 'PRO' as PackageType,
            name: 'Pro',
            price: '$9.99',
            description: 'For serious photographers',
            features: ['25 uploads per day', '25MB max file size', '500 total photos'],
            popular: true
        },
        {
            type: 'GOLD' as PackageType,
            name: 'Gold',
            price: '$29.99',
            description: 'Unlimited creative freedom',
            features: ['Unlimited uploads', '50MB max file size', 'Unlimited photos']
        }
    ];

    let usernameCheckTimeout: ReturnType<typeof setTimeout>;
    let emailCheckTimeout: ReturnType<typeof setTimeout>;

    async function checkUsername() {
        clearTimeout(usernameCheckTimeout);
        if (username.length < 3) {
            usernameError = '';
            return;
        }
        usernameCheckTimeout = setTimeout(async () => {
            try {
                const result = await authApi.checkUsername(username);
                usernameError = result.available ? '' : 'Username is already taken';
            } catch {
            }
        }, 500);
    }

    async function checkEmail() {
        clearTimeout(emailCheckTimeout);
        if (!email.includes('@')) {
            emailError = '';
            return;
        }
        emailCheckTimeout = setTimeout(async () => {
            try {
                const result = await authApi.checkEmail(email);
                emailError = result.available ? '' : 'Email is already in use';
            } catch {
                // Ignore errors during availability check
            }
        }, 500);
    }

    function validatePassword() {
        if (password && password.length < 6) {
            passwordError = 'Password must be at least 6 characters';
        } else if (confirmPassword && password !== confirmPassword) {
            passwordError = 'Passwords do not match';
        } else {
            passwordError = '';
        }
    }

    async function handleSubmit(e: Event) {
        e.preventDefault();

        if (usernameError || emailError || passwordError) return;
        if (password !== confirmPassword) {
            passwordError = 'Passwords do not match';
            return;
        }
        loading = true;
        error = '';
        try {
            await authStore.register(username, email, password, selectedPackage);
            goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Registration failed. Please try again.';
        } finally {
            loading = false;
        }
    }

    function handleOAuth(provider: 'google' | 'github' | 'okta') {
        window.location.href = `/api/oauth2/authorization/${provider}`;
    }
</script>
<svelte:head>
    <title>Create Account - InstaPound</title>
</svelte:head>
<div class="min-h-[calc(100vh-200px)] px-4 py-12">
    <div class="mx-auto max-w-4xl">
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
            <div class="mb-8 text-center">
                <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Create your account</h1>
                <p class="mt-2 text-slate-600 dark:text-slate-400">Join thousands of photographers sharing their work</p>
            </div>
            {#if error}
                <Alert variant="error" class="mb-6" dismissible>
                    {error}
                </Alert>
            {/if}
            <form onsubmit={handleSubmit}>
                <div class="mb-8">
                    <h2 class="mb-4 text-lg font-semibold text-slate-900 dark:text-white">Choose your plan</h2>
                    <div class="grid gap-4 md:grid-cols-3">
                        {#each packages as pkg}
                            <button
                                    type="button"
                                    onclick={() => (selectedPackage = pkg.type)}
                                    class="relative rounded-xl border-2 p-4 text-left transition-all
								{selectedPackage === pkg.type
									? 'border-violet-600 bg-violet-50 dark:border-violet-400 dark:bg-violet-950/50'
									: 'border-slate-200 hover:border-slate-300 dark:border-slate-700 dark:hover:border-slate-600'}"
                            >
                                {#if pkg.popular}
									<span class="absolute -top-3 left-1/2 -translate-x-1/2 rounded-full bg-violet-600 px-3 py-0.5 text-xs font-medium text-white">
										Popular
									</span>
                                {/if}
                                <div class="mb-2 flex items-center justify-between">
                                    <span class="text-lg font-semibold text-slate-900 dark:text-white">{pkg.name}</span>
                                    {#if selectedPackage === pkg.type}
                                        <div class="flex h-5 w-5 items-center justify-center rounded-full bg-violet-600 text-white">
                                            <Check class="h-3 w-3" />
                                        </div>
                                    {/if}
                                </div>
                                <p class="text-2xl font-bold text-slate-900 dark:text-white">{pkg.price}<span class="text-sm font-normal text-slate-500">/mo</span></p>
                                <p class="mt-1 text-sm text-slate-500">{pkg.description}</p>
                                <ul class="mt-3 space-y-1">
                                    {#each pkg.features as feature}
                                        <li class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
                                            <Check class="h-4 w-4 text-emerald-500" />
                                            {feature}
                                        </li>
                                    {/each}
                                </ul>
                            </button>
                        {/each}
                    </div>
                </div>

                <div class="mb-6 grid gap-5 md:grid-cols-2">
                    <Input
                            type="text"
                            label="Username"
                            placeholder="Choose a username"
                            bind:value={username}
                            error={usernameError}
                            oninput={checkUsername}
                            required
                    />
                    <Input
                            type="email"
                            label="Email"
                            placeholder="Enter your email"
                            bind:value={email}
                            error={emailError}
                            oninput={checkEmail}
                            required
                    />
                    <Input
                            type="password"
                            label="Password"
                            placeholder="Create a password"
                            bind:value={password}
                            error={passwordError}
                            oninput={validatePassword}
                            required
                    />
                    <Input
                            type="password"
                            label="Confirm Password"
                            placeholder="Confirm your password"
                            bind:value={confirmPassword}
                            oninput={validatePassword}
                            required
                    />
                </div>
                <Button type="submit" class="w-full" {loading} disabled={!!usernameError || !!emailError || !!passwordError}>
                    Create Account
                </Button>
            </form>
            <div class="relative my-6">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-slate-200 dark:border-slate-700"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="bg-white px-4 text-slate-500 dark:bg-slate-900 dark:text-slate-400">Or sign up with</span>
                </div>
            </div>
            <div class="grid grid-cols-1 gap-4">
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
                Already have an account?
                <a href="/login" class="font-medium text-violet-600 hover:text-violet-700 dark:text-violet-400 dark:hover:text-violet-300">
                    Sign in
                </a>
            </p>
        </Card>
    </div>
</div>