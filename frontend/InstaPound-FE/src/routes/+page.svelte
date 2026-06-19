<script lang="ts">
    import { onMount } from 'svelte';
    import { Camera, Sparkles, Zap, Shield, ArrowRight } from 'lucide-svelte';
    import { photosStore } from '$lib/stores/photo.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import PhotoGrid from '$lib/components/photos/PhotoGrid.svelte';
    import PhotoModal from '$lib/components/photos/PhotoModal.svelte';
    import Pagination from '$lib/components/ui/Pagination.svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import type { Photo } from '$lib/types';

    let selectedPhoto = $state<Photo | null>(null);

    onMount(() => {
        photosStore.browse(0, 12);
    });

    function handlePhotoClick(photo: Photo) {
        selectedPhoto = photo;
    }

    function handlePageChange(page: number) {
        photosStore.browse(page, 12);
        window.scrollTo({ top: 400, behavior: 'smooth' });
    }
</script>

<section class="relative overflow-hidden bg-gradient-to-br from-violet-600 via-indigo-600 to-purple-700">
    <div class="absolute inset-0 bg-[url('data:image/svg+xml,%3Csvg width=%2260%22 height=%2260%22 viewBox=%220 0 60 60%22 xmlns=%22http://www.w3.org/2000/svg%22%3E%3Cg fill=%22none%22 fill-rule=%22evenodd%22%3E%3Cg fill=%22%23ffffff%22 fill-opacity=%220.05%22%3E%3Cpath d=%22M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z%22/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')]"></div>
    <div class="relative mx-auto max-w-7xl px-4 py-24 sm:px-6 lg:px-8 lg:py-32">
        <div class="text-center">
            <h1 class="text-4xl font-bold tracking-tight text-white sm:text-5xl md:text-6xl lg:text-7xl">
                Share Your
                <span class="block bg-gradient-to-r from-amber-200 to-yellow-400 bg-clip-text text-transparent">
					Amazing Photos
				</span>
            </h1>
            <p class="mx-auto mt-6 max-w-2xl text-lg text-violet-100 sm:text-xl">
                Upload, browse, and share stunning photos with the world. Choose the perfect package for your creative journey.
            </p>
            <div class="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
                {#if authStore.isAuthenticated}
                    <a href="/upload">
                        <Button size="lg" class="bg-white text-violet-600 hover:bg-violet-50">
                            <Camera class="h-5 w-5" />
                            Upload Photo
                        </Button>
                    </a>
                {:else}
                    <a href="/register" class="block">
                        <Button size="lg" class="bg-white text-violet-600 hover:bg-violet-50">
                            Get Started Free
                            <ArrowRight class="h-5 w-5" />
                        </Button>
                    </a>
                {/if}
                <a href="/pricing">
                    <Button size="lg" variant="outline" class="border-white/30 text-white hover:bg-white/10">
                        View Pricing
                    </Button>
                </a>
            </div>
        </div>
    </div>

    <div class="absolute bottom-0 left-0 right-0 pointer-events-none">
        <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 120L60 105C120 90 240 60 360 45C480 30 600 30 720 37.5C840 45 960 60 1080 67.5C1200 75 1320 75 1380 75L1440 75V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z" class="fill-slate-50 dark:fill-slate-950"/>
        </svg>
    </div>
</section>

<section class="py-16">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div class="grid gap-8 md:grid-cols-3">
            <div class="rounded-2xl bg-white p-8 shadow-lg shadow-slate-200/50 dark:bg-slate-900 dark:shadow-slate-900/50">
                <div class="mb-4 inline-flex rounded-xl bg-violet-100 p-3 dark:bg-violet-900/50">
                    <Sparkles class="h-6 w-6 text-violet-600 dark:text-violet-400" />
                </div>
                <h3 class="mb-2 text-lg font-semibold text-slate-900 dark:text-white">Easy Upload</h3>
                <p class="text-slate-600 dark:text-slate-400">
                    Upload photos in seconds with hashtags, descriptions, and automatic thumbnail generation.
                </p>
            </div>
            <div class="rounded-2xl bg-white p-8 shadow-lg shadow-slate-200/50 dark:bg-slate-900 dark:shadow-slate-900/50">
                <div class="mb-4 inline-flex rounded-xl bg-emerald-100 p-3 dark:bg-emerald-900/50">
                    <Zap class="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
                </div>
                <h3 class="mb-2 text-lg font-semibold text-slate-900 dark:text-white">Smart Processing</h3>
                <p class="text-slate-600 dark:text-slate-400">
                    Apply filters, resize, and convert formats before uploading or downloading.
                </p>
            </div>
            <div class="rounded-2xl bg-white p-8 shadow-lg shadow-slate-200/50 dark:bg-slate-900 dark:shadow-slate-900/50">
                <div class="mb-4 inline-flex rounded-xl bg-amber-100 p-3 dark:bg-amber-900/50">
                    <Shield class="h-6 w-6 text-amber-600 dark:text-amber-400" />
                </div>
                <h3 class="mb-2 text-lg font-semibold text-slate-900 dark:text-white">Flexible Plans</h3>
                <p class="text-slate-600 dark:text-slate-400">
                    Choose from FREE, PRO, or GOLD packages based on your storage and upload needs.
                </p>
            </div>
        </div>
    </div>
</section>

<section class="pb-20">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div class="mb-8 flex items-center justify-between">
            <div>
                <h2 class="text-2xl font-bold text-slate-900 dark:text-white">Latest Photos</h2>
                <p class="mt-1 text-slate-600 dark:text-slate-400">Discover amazing photos from our community</p>
            </div>
            <a href="/search" class="text-sm font-medium text-violet-600 transition-colors hover:text-violet-700 dark:text-violet-400 dark:hover:text-violet-300">
                Search all photos →
            </a>
        </div>
        <PhotoGrid
                photos={photosStore.photos}
                loading={photosStore.loading}
                onPhotoClick={handlePhotoClick}
        />
        {#if photosStore.totalPages > 1}
            <div class="mt-12">
                <Pagination
                        currentPage={photosStore.page}
                        totalPages={photosStore.totalPages}
                        onPageChange={handlePageChange}
                />
            </div>
        {/if}
    </div>
</section>

{#if selectedPhoto}
    <PhotoModal
            photo={selectedPhoto}
            onclose={() => (selectedPhoto = null)}
    />
{/if}