<script lang="ts">
    import { onMount } from 'svelte';
    import { User, Calendar, Camera, Package, ArrowRight, AlertCircle, Crown, Clock, Database, Upload } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import PhotoGrid from '$lib/components/photos/PhotoGrid.svelte';
    import PhotoModal from '$lib/components/photos/PhotoModal.svelte';
    import Pagination from '$lib/components/ui/Pagination.svelte';
    import Spinner from '$lib/components/ui/Spinner.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import { packageStore } from '$lib/stores/packages.svelte';
    import { photosApi } from '$lib/api';
    import { goto } from '$app/navigation';
    import type { Photo, Page } from '$lib/types';
    
    let photosData = $state<Page<Photo> | null>(null);
    let loading = $state(true);
    let selectedPhoto = $state<Photo | null>(null);

    onMount(async () => {
        if (!authStore.isAuthenticated) {
            goto('/login');
            return;
        }
        await Promise.all([loadPhotos(), packageStore.loadCurrentPackage()]);
        loading = false;
    });

    async function loadPhotos(page = 0) {
        if (!authStore.user) return;
        try {
            photosData = await photosApi.getByUser(authStore.user.id, page, 8);
        } catch {

        }
    }

    function formatDate(dateString: string | null): string {
        if (!dateString) return 'Never';
        return new Date(dateString).toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    }

    function formatBytes(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
    }

    function getUsagePercentage(current: number, max: number): number {
        if (max === 0 || max === -1) return 0;
        return Math.min((current / max) * 100, 100);
    }

    function handlePhotoClick(photo: Photo) {
        selectedPhoto = photo;
    }
</script>

<svelte:head>
    <title>My Profile - InstaPound</title>
</svelte:head>

{#if loading}
    <div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <Spinner size="lg" />
    </div>
{:else if authStore.user}
    <div class="mx-auto max-w-7xl px-4 py-12">
        <div class="mb-8 flex flex-col items-center gap-6 sm:flex-row sm:items-start">
            <div class="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-3xl font-bold text-white shadow-lg shadow-violet-500/25">
                {authStore.user.username.charAt(0).toUpperCase()}
            </div>
            <div class="flex-1 text-center sm:text-left">
                <h1 class="text-3xl font-bold text-slate-900 dark:text-white">{authStore.user.username}</h1>
                <p class="mt-1 text-slate-600 dark:text-slate-400">{authStore.user.email}</p>
                <div class="mt-3 flex flex-wrap items-center justify-center gap-4 text-sm text-slate-500 dark:text-slate-400 sm:justify-start">
					<span class="flex items-center gap-1.5">
						<Calendar class="h-4 w-4" />
						Joined {formatDate(authStore.user.createdAt)}
					</span>
                    <span class="flex items-center gap-1.5">
						<Crown class="h-4 w-4 text-amber-500" />
                        {authStore.user.packageType} Plan
					</span>
                    {#if authStore.isAdmin}
						<span class="flex items-center gap-1.5 rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700 dark:bg-red-900/50 dark:text-red-400">
							Admin
						</span>
                    {/if}
                </div>
            </div>
            <div class="flex gap-3">
                <a href="/upload">
                    <Button>
                        <Upload class="h-4 w-4" />
                        Upload
                    </Button>
                </a>
                <a href="/pricing">
                    <Button variant="outline">
                        <Package class="h-4 w-4" />
                        Upgrade
                    </Button>
                </a>
            </div>
        </div>

        {#if packageStore.currentPackage}
            <Card variant="elevated" padding="lg" class="mb-8">
                <div class="flex items-center justify-between mb-6">
                    <h2 class="text-lg font-semibold text-slate-900 dark:text-white">Package Usage</h2>
                    <a href="/pricing" class="text-sm font-medium text-violet-600 hover:text-violet-700 dark:text-violet-400">
                        View plans <ArrowRight class="inline h-4 w-4" />
                    </a>
                </div>
                {#if authStore.user.pendingPackageType && authStore.user.packageChangeEffectiveDate !== undefined}
                    <Alert variant="info" class="mb-6">
                        <strong>Package change pending:</strong> Your plan will change to
                        {authStore.user.pendingPackageType} on
                        {formatDate(authStore.user.packageChangeEffectiveDate)}.
                    </Alert>
                {/if}
                <div class="grid gap-6 sm:grid-cols-3">
                    <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                        <div class="mb-3 flex items-center justify-between">
							<span class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
								<Upload class="h-4 w-4" />
								Daily Uploads
							</span>
                            <span class="text-sm font-medium text-slate-900 dark:text-white">
								{packageStore.currentPackage.currentUsage?.uploadedToday || 0} /
                                {packageStore.currentPackage.limits.dailyUploadLimit === -1 ? '∞' : packageStore.currentPackage.limits.dailyUploadLimit}
							</span>
                        </div>
                        <div class="h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
                            <div
                                    class="h-full rounded-full bg-gradient-to-r from-violet-500 to-indigo-500 transition-all"
                                    style="width: {packageStore.currentPackage.limits.dailyUploadLimit === -1 ? 0 :
                                    getUsagePercentage(packageStore.currentPackage.currentUsage?.uploadedToday || 0,
                                    packageStore.currentPackage.limits.dailyUploadLimit)}%"
                            ></div>
                        </div>
                    </div>

                    <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                        <div class="mb-3 flex items-center justify-between">
							<span class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
								<Camera class="h-4 w-4" />
								Total Photos
							</span>
                            <span class="text-sm font-medium text-slate-900 dark:text-white">
								{packageStore.currentPackage.currentUsage?.currentPhotoCount || 0} / {packageStore.currentPackage.limits.maxTotalPhotos === -1 ? '∞' : packageStore.currentPackage.limits.maxTotalPhotos}
							</span>
                        </div>
                        <div class="h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
                            <div
                                    class="h-full rounded-full bg-gradient-to-r from-emerald-500 to-teal-500 transition-all"
                                    style="width: {packageStore.currentPackage.limits.maxTotalPhotos === -1 ? 0 : getUsagePercentage(packageStore.currentPackage.currentUsage?.currentPhotoCount || 0, packageStore.currentPackage.limits.maxTotalPhotos)}%"
                            ></div>
                        </div>
                    </div>

                    <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                        <div class="mb-3 flex items-center justify-between">
							<span class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
								<Database class="h-4 w-4" />
								Storage Used
							</span>
                            <span class="text-sm font-medium text-slate-900 dark:text-white">
								{formatBytes(packageStore.currentPackage.currentUsage?.totalStorageUsed || 0)}
							</span>
                        </div>
                        <div class="h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700">
                            <div class="h-full w-0 rounded-full bg-gradient-to-r from-amber-500 to-orange-500"></div>
                        </div>
                    </div>
                </div>
                <div class="mt-4 text-sm text-slate-500 dark:text-slate-400">
					<span class="flex items-center gap-1.5">
						<Clock class="h-4 w-4" />
						Max upload size: {formatBytes(packageStore.currentPackage.limits.maxUploadSizeInBytes)}
					</span>
                </div>
            </Card>
        {/if}

        <div>
            <div class="mb-6 flex items-center justify-between">
                <h2 class="text-xl font-bold text-slate-900 dark:text-white">My Photos</h2>
                {#if photosData && photosData.totalElements > 0}
                    <span class="text-sm text-slate-500">{photosData.totalElements} photos</span>
                {/if}
            </div>
            {#if photosData}
                <PhotoGrid
                        photos={photosData.content}
                        loading={false}
                        onPhotoClick={handlePhotoClick}
                />
                {#if photosData.totalPages > 1}
                    <div class="mt-12">
                        <Pagination
                                currentPage={photosData.number}
                                totalPages={photosData.totalPages}
                                onPageChange={(page) => loadPhotos(page)}
                        />
                    </div>
                {/if}
            {/if}
        </div>
    </div>

    {#if selectedPhoto}
        <PhotoModal
                photo={selectedPhoto}
                onclose={() => (selectedPhoto = null)}
                ondelete={async () => {
				if (selectedPhoto) {
					await photosApi.delete(selectedPhoto.id);
					selectedPhoto = null;
					await loadPhotos();
				}
			}}
        />
        <!--                onedit={() => {-->
        <!--                    -->
        <!--			}}-->
    {/if}
{:else}
    <div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <Card variant="elevated" padding="lg" class="text-center">
            <AlertCircle class="mx-auto mb-4 h-12 w-12 text-amber-500" />
            <h2 class="mb-2 text-xl font-semibold text-slate-900 dark:text-white">Not signed in</h2>
            <p class="mb-4 text-slate-600 dark:text-slate-400">Please sign in to view your profile</p>
            <a href="/login">
                <Button>Sign In</Button>
            </a>
        </Card>
    </div>
{/if}