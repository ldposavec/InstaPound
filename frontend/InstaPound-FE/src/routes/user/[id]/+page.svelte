<script lang="ts">
    import { page } from '$app/state';
    import { onMount } from 'svelte';
    import { ArrowLeft, Calendar, Camera, Crown, User } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import PhotoGrid from '$lib/components/photos/PhotoGrid.svelte';
    import PhotoModal from '$lib/components/photos/PhotoModal.svelte';
    import Pagination from '$lib/components/ui/Pagination.svelte';
    import Spinner from '$lib/components/ui/Spinner.svelte';
    import { photosApi, adminApi } from '$lib/api';
    import { authStore } from '$lib/stores/auth.svelte';
    import type { Photo, Page, User as UserType } from '$lib/types';

    let user = $state<UserType | null>(null);
    let photosData = $state<Page<Photo> | null>(null);
    let loading = $state(true);
    let error = $state('');
    let selectedPhoto = $state<Photo | null>(null);

    onMount(async () => {
        const id = parseInt(page.params.id);
        if (isNaN(id)) {
            error = 'Invalid user ID';
            loading = false;
            return;
        }
        try {
            if (authStore.isAdmin) {
                user = await adminApi.getUserById(id);
            }
            photosData = await photosApi.getByUser(id, 0, 12);

            if (!user && photosData.content.length > 0) {
                user = {
                    id,
                    username: photosData.content[0].author,
                    email: '',
                    role: 'REGISTERED',
                    packageType: 'FREE',
                    packageUsage: null,
                    pendingPackageType: null,
                    packageChangeEffectiveDate: null,
                    createdAt: '',
                    lastLoginAt: null
                };
            }
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to load user';
        } finally {
            loading = false;
        }
    });

    async function loadPhotos(pageNum: number) {
        const id = parseInt(page.params.id);
        if (isNaN(id)) return;
        try {
            photosData = await photosApi.getByUser(id, pageNum, 12);
        } catch {
        }
    }

    function handlePhotoClick(photo: Photo) {
        selectedPhoto = photo;
    }
</script>

<svelte:head>
    <title>{user?.username || 'User'}'s Photos - InstaPound</title>
</svelte:head>

{#if loading}
    <div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <Spinner size="lg" />
    </div>
{:else if error || !user}
    <div class="mx-auto max-w-2xl px-4 py-20 text-center">
        <Card variant="elevated" padding="lg">
            <User class="mx-auto mb-4 h-16 w-16 text-slate-300" />
            <h1 class="mb-2 text-2xl font-bold text-slate-900 dark:text-white">User not found</h1>
            <p class="mb-6 text-slate-600 dark:text-slate-400">
                {error || 'The user you\'re looking for doesn\'t exist.'}
            </p>
            <a href="/">
                <Button>
                    <ArrowLeft class="h-4 w-4" />
                    Back to Home
                </Button>
            </a>
        </Card>
    </div>
{:else}
    <div class="mx-auto max-w-7xl px-4 py-12">
        <Card variant="elevated" padding="lg" class="mb-8">
            <div class="flex flex-col items-center gap-6 sm:flex-row sm:items-start">
                <div class="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-3xl font-bold text-white shadow-lg shadow-violet-500/25">
                    {user.username.charAt(0).toUpperCase()}
                </div>
                <div class="flex-1 text-center sm:text-left">
                    <h1 class="text-3xl font-bold text-slate-900 dark:text-white">{user.username}</h1>
                    <div class="mt-3 flex flex-wrap items-center justify-center gap-4 text-sm text-slate-500 dark:text-slate-400 sm:justify-start">
                        {#if user.createdAt}
							<span class="flex items-center gap-1.5">
								<Calendar class="h-4 w-4" />
								Joined {new Date(user.createdAt).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
							</span>
                        {/if}
                        <span class="flex items-center gap-1.5">
							<Camera class="h-4 w-4" />
                            {photosData?.totalElements || 0} photos
						</span>
                        {#if authStore.isAdmin}
							<span class="flex items-center gap-1.5">
								<Crown class="h-4 w-4 text-amber-500" />
                                {user.packageType}
							</span>
                        {/if}
                    </div>
                </div>
            </div>
        </Card>

        <div class="mb-6 flex items-center justify-between">
            <h2 class="text-xl font-bold text-slate-900 dark:text-white">Photos</h2>
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
                            onPageChange={loadPhotos}
                    />
                </div>
            {/if}
        {/if}
    </div>

    {#if selectedPhoto}
        <PhotoModal photo={selectedPhoto} onclose={() => (selectedPhoto = null)} />
    {/if}
{/if}