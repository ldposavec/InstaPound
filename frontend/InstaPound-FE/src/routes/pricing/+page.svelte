<script lang="ts">
    import { onMount } from 'svelte';
    import { Check, Crown, Sparkles, Star, X, ArrowRight, CreditCard } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import Modal from '$lib/components/ui/Modal.svelte';
    import Spinner from '$lib/components/ui/Spinner.svelte';
    import { packageStore } from '$lib/stores/packages.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import { loadStripe } from '@stripe/stripe-js';
    import type { PackageType } from '$lib/types';
    // import { PUBLIC_STRIPE_KEY } from '$env/static/public'

    let loading = $state(true);
    let showConfirmModal = $state(false);
    let selectedPackageType = $state<PackageType | null>(null);
    let changeLoading = $state(false);
    let changeError = $state('');
    let changeSuccess = $state('');
    let showStripeModal = $state(false);

    // const STRIPE_PUBLIC_KEY = PUBLIC_STRIPE_KEY || '';
    const STRIPE_PUBLIC_KEY = '';

    const packages = [
        {
            type: 'FREE' as PackageType,
            name: 'Free',
            price: 0,
            priceLabel: '$0',
            period: '/forever',
            description: 'Perfect for beginners',
            icon: Sparkles,
            iconBg: 'bg-slate-100 dark:bg-slate-800',
            iconColor: 'text-slate-600 dark:text-slate-400',
            features: [
                { text: '5 uploads per day', included: true },
                { text: '10MB max file size', included: true },
                { text: '50 total photos', included: true },
                { text: 'Basic filters', included: true },
                { text: 'Priority support', included: false },
                { text: 'Cloud storage', included: false }
            ]
        },
        {
            type: 'PRO' as PackageType,
            name: 'Pro',
            price: 9.99,
            priceLabel: '$9.99',
            period: '/month',
            description: 'For serious photographers',
            icon: Crown,
            iconBg: 'bg-violet-100 dark:bg-violet-900/50',
            iconColor: 'text-violet-600 dark:text-violet-400',
            popular: true,
            features: [
                { text: '25 uploads per day', included: true },
                { text: '25MB max file size', included: true },
                { text: '500 total photos', included: true },
                { text: 'All filters', included: true },
                { text: 'Priority support', included: true },
                { text: 'Cloud storage', included: false }
            ]
        },
        {
            type: 'GOLD' as PackageType,
            name: 'Gold',
            price: 19.99,
            priceLabel: '$19.99',
            period: '/month',
            description: 'Unlimited creative freedom',
            icon: Star,
            iconBg: 'bg-amber-100 dark:bg-amber-900/50',
            iconColor: 'text-amber-600 dark:text-amber-400',
            features: [
                { text: 'Unlimited uploads', included: true },
                { text: '50MB max file size', included: true },
                { text: 'Unlimited photos', included: true },
                { text: 'All filters', included: true },
                { text: 'Priority support', included: true },
                { text: 'Cloud storage', included: true }
            ]
        }
    ];

    onMount(async () => {
        await packageStore.loadPackages();
        loading = false;
    });

    function handleSelectPackage(pkgType: PackageType) {
        if (!authStore.isAuthenticated) {
            window.location.href = '/register';
            return;
        }
        if (authStore.user?.packageType === pkgType) return;
        selectedPackageType = pkgType;

        const pkg = packages.find((p) => p.type === pkgType);
        if (pkg && pkg.price > 0) {
            showStripeModal = true;
        } else {
            showConfirmModal = true;
        }
    }

    async function handleConfirmChange() {
        if (!selectedPackageType) return;

        changeLoading = true;
        changeError = '';
        changeSuccess = '';
        try {
            await packageStore.changePackage(selectedPackageType);
            changeSuccess = 'Package change requested! Your new plan will be active tomorrow.';
            showConfirmModal = false;
            showStripeModal = false;
            await authStore.refresh();
        } catch (err) {
            changeError = err instanceof Error ? err.message : 'Failed to change package';
        } finally {
            changeLoading = false;
        }
    }

    async function handleStripeCheckout() {
        const pkg = packages.find((p) => p.type === selectedPackageType);
        if (!pkg) return;
        try {
            // In a real implementation, you would:
            // 1. Call your backend to create a Stripe Checkout session
            // 2. Redirect to Stripe Checkout
            // For demo purposes, we'll just confirm the change
            // const stripe = await loadStripe(STRIPE_PUBLIC_KEY);
            // const response = await fetch('/api/create-checkout-session', {
            //   method: 'POST',
            //   headers: { 'Content-Type': 'application/json' },
            //   body: JSON.stringify({ packageType: selectedPackageType })
            // });
            // const { sessionId } = await response.json();
            // await stripe?.redirectToCheckout({ sessionId });
            // For now, just confirm the change
            await handleConfirmChange();
        } catch (err) {
            changeError = 'Payment processing failed. Please try again.';
        }
    }

    async function handleCancelPending() {
        changeLoading = true;
        changeError = '';
        try {
            await packageStore.cancelChange();
            await authStore.refresh();
        } catch (err) {
            changeError = err instanceof Error ? err.message : 'Failed to cancel';
        } finally {
            changeLoading = false;
        }
    }

    function getColorClasses(color: string, isSelected: boolean) {
        const colors: Record<string, { bg: string; border: string; button: string }> = {
            slate: {
                bg: isSelected ? 'bg-slate-50 dark:bg-slate-800/50' : '',
                border: isSelected ? 'border-slate-400 dark:border-slate-500' : 'border-slate-200 dark:border-slate-700',
                button: 'bg-slate-900 hover:bg-slate-800 dark:bg-slate-100 dark:hover:bg-slate-200 dark:text-slate-900'
            },
            violet: {
                bg: isSelected ? 'bg-violet-50 dark:bg-violet-950/50' : '',
                border: isSelected ? 'border-violet-500' : 'border-slate-200 dark:border-slate-700',
                button: 'bg-violet-600 hover:bg-violet-700'
            },
            amber: {
                bg: isSelected ? 'bg-amber-50 dark:bg-amber-950/50' : '',
                border: isSelected ? 'border-amber-500' : 'border-slate-200 dark:border-slate-700',
                button: 'bg-amber-600 hover:bg-amber-700'
            }
        };
        return colors[color] || colors.slate;
    }
</script>

<svelte:head>
    <title>Pricing - InstaPound</title>
</svelte:head>

<div class="bg-gradient-to-b from-violet-50 to-white dark:from-slate-900 dark:to-slate-950">
    <div class="mx-auto max-w-7xl px-4 py-16 text-center sm:px-6 lg:px-8">
        <h1 class="text-4xl font-bold tracking-tight text-slate-900 dark:text-white sm:text-5xl">
            Simple, transparent pricing
        </h1>
        <p class="mx-auto mt-4 max-w-2xl text-lg text-slate-600 dark:text-slate-400">
            Choose the perfect plan for your photography needs. Upgrade or downgrade anytime.
        </p>
    </div>

    <div class="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
        {#if changeSuccess}
            <Alert variant="success" class="mb-6" dismissible>
                {changeSuccess}
            </Alert>
        {/if}
        {#if changeError}
            <Alert variant="error" class="mb-6" dismissible>
                {changeError}
            </Alert>
        {/if}
        {#if authStore.user?.pendingPackageType}
            <Alert variant="info" class="mb-6">
                <div class="flex items-center justify-between">
					<span>
						<strong>Pending change:</strong> Your plan will change to {authStore.user.pendingPackageType} tomorrow.
					</span>
                    <button
                            onclick={handleCancelPending}
                            class="ml-4 text-sm font-medium underline hover:no-underline"
                            disabled={changeLoading}
                    >
                        Cancel
                    </button>
                </div>
            </Alert>
        {/if}
    </div>

    <div class="mx-auto max-w-7xl px-4 pb-24 sm:px-6 lg:px-8">
        {#if loading}
            <div class="flex justify-center py-20">
                <Spinner size="lg" />
            </div>
        {:else}
            <div class="grid gap-8 lg:grid-cols-3">
                {#each packages as pkg}
                    {@const isCurrentPlan = authStore.user?.packageType === pkg.type}
                    {@const isPending = authStore.user?.pendingPackageType === pkg.type}
                    {@const colors = getColorClasses(pkg.type === 'FREE'
                            ? 'slate' : pkg.type === 'PRO' ? 'violet' : 'amber', isCurrentPlan)}
                    <div class="relative">
                        {#if pkg.popular}
                            <div class="absolute -top-4 left-1/2 z-10 -translate-x-1/2">
								<span class="rounded-full bg-gradient-to-r from-violet-600 to-indigo-600 px-4 py-1 text-sm font-medium text-white shadow-lg">
									Most Popular
								</span>
                            </div>
                        {/if}
                        <Card
                                variant="bordered"
                                padding="none"
                                class="h-full {colors.border} {colors.bg} {pkg.popular ? 'ring-2 ring-violet-500' : ''}"
                        >
                            <div class="p-8">
                                <div class="mb-4 flex items-center gap-3">
<!--                                    <div class="rounded-xl bg-{pkg.color}-100 dark:bg-{pkg.color}-900/50 p-3">-->
<!--                                        <svelte:component-->
<!--                                                this={pkg.icon}-->
<!--                                                class="h-6 w-6 text-{pkg.color}-600 dark:text-{pkg.color}-400"-->
<!--                                        />-->
<!--                                    </div>-->
                                    <div class="rounded-xl p-3 {pkg.iconBg}">
                                        <svelte:component
                                                this={pkg.icon}
                                                class="h-6 w-6 {pkg.iconColor}"
                                        />
                                    </div>
                                    <div>
                                        <h3 class="text-xl font-bold text-slate-900 dark:text-white">{pkg.name}</h3>
                                        <p class="text-sm text-slate-500">{pkg.description}</p>
                                    </div>
                                </div>
                                <div class="mb-6">
                                    <span class="text-4xl font-bold text-slate-900 dark:text-white">{pkg.priceLabel}</span>
                                    <span class="text-slate-500">{pkg.period}</span>
                                </div>
                                <ul class="mb-8 space-y-4">
                                    {#each pkg.features as feature}
                                        <li class="flex items-center gap-3">
                                            {#if feature.included}
                                                <div class="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-100 dark:bg-emerald-900/50">
                                                    <Check class="h-3 w-3 text-emerald-600 dark:text-emerald-400" />
                                                </div>
                                            {:else}
                                                <div class="flex h-5 w-5 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-800">
                                                    <X class="h-3 w-3 text-slate-400" />
                                                </div>
                                            {/if}
                                            <span class="{feature.included ? 'text-slate-700 dark:text-slate-300' : 'text-slate-400 dark:text-slate-500'}">
												{feature.text}
											</span>
                                        </li>
                                    {/each}
                                </ul>
                                {#if isCurrentPlan}
                                    <div class="rounded-lg border-2 border-emerald-500 bg-emerald-50 py-3 text-center font-medium text-emerald-700 dark:border-emerald-400 dark:bg-emerald-900/50 dark:text-emerald-400">
                                        Current Plan
                                    </div>
                                {:else if isPending}
                                    <div class="rounded-lg border-2 border-amber-500 bg-amber-50 py-3 text-center font-medium text-amber-700 dark:border-amber-400 dark:bg-amber-900/50 dark:text-amber-400">
                                        Pending
                                    </div>
                                {:else}
                                    <button
                                            onclick={() => handleSelectPackage(pkg.type)}
                                            class="w-full rounded-lg py-3 text-center font-medium text-white transition-colors {colors.button}"
                                    >
                                        {pkg.price > 0 ? 'Subscribe' : 'Get Started'}
                                        <ArrowRight class="ml-1 inline h-4 w-4" />
                                    </button>
                                {/if}
                            </div>
                        </Card>
                    </div>
                {/each}
            </div>
        {/if}
    </div>
</div>

<Modal bind:open={showConfirmModal} title="Confirm Package Change" size="sm">
    <p class="mb-6 text-slate-600 dark:text-slate-400">
        Are you sure you want to change to the <strong>{selectedPackageType}</strong> package?
        The change will take effect tomorrow.
    </p>
    <div class="flex gap-3">
        <Button variant="secondary" class="flex-1" onclick={() => (showConfirmModal = false)}>
            Cancel
        </Button>
        <Button class="flex-1" loading={changeLoading} onclick={handleConfirmChange}>
            Confirm
        </Button>
    </div>
</Modal>

<Modal bind:open={showStripeModal} title="Complete Payment" size="md">
    {@const pkg = packages.find((p) => p.type === selectedPackageType)}
    {#if pkg}
        <div class="mb-6">
            <div class="mb-4 rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                <div class="flex items-center justify-between">
                    <span class="font-medium text-slate-900 dark:text-white">{pkg.name} Plan</span>
                    <span class="text-xl font-bold text-slate-900 dark:text-white">{pkg.priceLabel}/mo</span>
                </div>
            </div>
            <p class="mb-4 text-sm text-slate-600 dark:text-slate-400">
                You will be charged {pkg.priceLabel} monthly. Your plan will be active starting tomorrow.
                You can cancel anytime.
            </p>
            <div class="rounded-lg border border-slate-200 p-4 dark:border-slate-700">
                <div class="mb-3 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                    <CreditCard class="h-4 w-4" />
                    Secure payment powered by Stripe
                </div>
                <p class="text-xs text-slate-500">
                    For demo purposes, clicking "Pay Now" will activate the plan without actual payment processing.
                </p>
            </div>
        </div>
        <div class="flex gap-3">
            <Button variant="secondary" class="flex-1" onclick={() => (showStripeModal = false)}>
                Cancel
            </Button>
            <Button class="flex-1" loading={changeLoading} onclick={handleStripeCheckout}>
                <CreditCard class="h-4 w-4" />
                Pay {pkg.priceLabel}
            </Button>
        </div>
    {/if}
</Modal>