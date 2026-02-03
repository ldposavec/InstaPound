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
			price: '$19.99',
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
				// Ignore errors during availability check
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
		<!-- Logo -->
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
				<!-- Package Selection -->
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

				<!-- Account Details -->
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

			<div class="grid grid-cols-3 gap-4">
				<button
					onclick={() => handleOAuth('google')}
					class="flex items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
				>
					<svg class="h-5 w-5" viewBox="0 0 24 24">
						<path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
						<path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
						<path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
						<path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
					</svg>
					Google
				</button>
				<button
					onclick={() => handleOAuth('github')}
					class="flex items-center justify-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
				>
					<svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor">
						<path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
					</svg>
					GitHub
				</button>
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
