<script lang="ts">
    import { Search, Filter, X, Calendar, User, Hash, FileImage } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import PhotoGrid from '$lib/components/photos/PhotoGrid.svelte';
    import PhotoModal from '$lib/components/photos/PhotoModal.svelte';
    import Pagination from '$lib/components/ui/Pagination.svelte';
    import Hashtag from "$lib/components/ui/Hashtag.svelte";
    import { photosStore } from '$lib/stores/photo.svelte';
    import type { Photo, PhotoSearchRequest } from '$lib/types';
    let selectedPhoto = $state<Photo | null>(null);
    let showFilters = $state(false);
    let searched = $state(false);

    let hashtagInput = $state('');
    let hashtags = $state<string[]>([]);
    let author = $state('');
    let uploadedAfter = $state('');
    let uploadedBefore = $state('');
    let minSizeMB = $state<number | ''>('');
    let maxSizeMB = $state<number | ''>('');

    function addHashtag() {
        const tag = hashtagInput.trim().toLowerCase().replace(/^#/, '');
        if (tag && !hashtags.includes(tag)) {
            hashtags = [...hashtags, tag];
        }
        hashtagInput = '';
    }

    function removeHashtag(tag: string) {
        hashtags = hashtags.filter((t) => t !== tag);
    }

    function handleHashtagKeydown(e: KeyboardEvent) {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault();
            addHashtag();
        }
    }

    async function handleSearch(e?: Event) {
        e?.preventDefault();
        const request: PhotoSearchRequest = {
            page: 0,
            pageSize: 12
        };

        if (hashtags.length > 0) request.hashtags = hashtags;
        if (author.trim()) request.author = author.trim();
        if (uploadedAfter) request.uploadedAfter = new Date(uploadedAfter).toISOString();
        if (uploadedBefore) request.uploadedBefore = new Date(uploadedBefore).toISOString();
        if (minSizeMB) request.minSizeBytes = Number(minSizeMB) * 1024 * 1024;
        if (maxSizeMB) request.maxSizeBytes = Number(maxSizeMB) * 1024 * 1024;

        await photosStore.search(request);
        searched = true;
    }

    async function handlePageChange(page: number) {
        const request: PhotoSearchRequest = {
            page,
            pageSize: 12
        };

        if (hashtags.length > 0) request.hashtags = hashtags;
        if (author.trim()) request.author = author.trim();
        if (uploadedAfter) request.uploadedAfter = new Date(uploadedAfter).toISOString();
        if (uploadedBefore) request.uploadedBefore = new Date(uploadedBefore).toISOString();
        if (minSizeMB) request.minSizeBytes = Number(minSizeMB) * 1024 * 1024;
        if (maxSizeMB) request.maxSizeBytes = Number(maxSizeMB) * 1024 * 1024;

        await photosStore.search(request);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function clearFilters() {
        hashtags = [];
        hashtagInput = '';
        author = '';
        uploadedAfter = '';
        uploadedBefore = '';
        minSizeMB = '';
        maxSizeMB = '';
    }

    function handlePhotoClick(photo: Photo) {
        selectedPhoto = photo;
    }

    const hasFilters = $derived(
        hashtags.length > 0 || author || uploadedAfter || uploadedBefore || minSizeMB || maxSizeMB
    );
</script>

<svelte:head>
    <title>Search Photos - InstaPound</title>
</svelte:head>

<div class="mx-auto max-w-7xl px-4 py-12">
    <div class="mb-8 text-center">
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Search Photos</h1>
        <p class="mt-2 text-slate-600 dark:text-slate-400">
            Find photos by hashtags, author, date, or size
        </p>
    </div>

    <Card variant="elevated" padding="lg" class="mb-8">
        <form onsubmit={handleSearch}>
            <div class="flex gap-3">
                <div class="relative flex-1">
                    <Hash class="absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                    <input
                            type="text"
                            placeholder="Search by hashtag..."
                            bind:value={hashtagInput}
                            onkeydown={handleHashtagKeydown}
                            class="w-full rounded-xl border border-slate-200 bg-white py-3 pl-12 pr-4 text-slate-900 transition-colors placeholder:text-slate-400 focus:border-violet-500 focus:outline-none focus:ring-2 focus:ring-violet-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
                    />
                </div>
                <Button type="button" variant="secondary" onclick={() => (showFilters = !showFilters)}>
                    <Filter class="h-5 w-5" />
                    Filters
                </Button>
                <Button type="submit" loading={photosStore.loading}>
                    <Search class="h-5 w-5" />
                    Search
                </Button>
            </div>

            {#if hashtags.length > 0}
                <div class="mt-4 flex flex-wrap gap-2">
                    {#each hashtags as tag}
                        <Hashtag {tag} removable onremove={() => removeHashtag(tag)} />
                    {/each}
                </div>
            {/if}

            {#if showFilters}
                <div class="mt-6 grid gap-4 border-t border-slate-200 pt-6 dark:border-slate-700 sm:grid-cols-2 lg:grid-cols-4">
                    <div>
                        <label class="mb-1.5 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                            <User class="h-4 w-4" />
                            Author
                        </label>
                        <Input type="text" placeholder="Username" bind:value={author} />
                    </div>
                    <div>
                        <label class="mb-1.5 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                            <Calendar class="h-4 w-4" />
                            Uploaded After
                        </label>
                        <Input type="date" bind:value={uploadedAfter} />
                    </div>
                    <div>
                        <label class="mb-1.5 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                            <Calendar class="h-4 w-4" />
                            Uploaded Before
                        </label>
                        <Input type="date" bind:value={uploadedBefore} />
                    </div>
                    <div>
                        <label class="mb-1.5 flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
                            <FileImage class="h-4 w-4" />
                            File Size (MB)
                        </label>
                        <div class="flex items-center gap-2">
                            <Input type="number" placeholder="Min" bind:value={minSizeMB} />
                            <span class="text-slate-400">-</span>
                            <Input type="number" placeholder="Max" bind:value={maxSizeMB} />
                        </div>
                    </div>
                </div>
                {#if hasFilters}
                    <div class="mt-4 flex justify-end">
                        <button
                                type="button"
                                onclick={clearFilters}
                                class="flex items-center gap-1.5 text-sm text-slate-500 transition-colors hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-300"
                        >
                            <X class="h-4 w-4" />
                            Clear filters
                        </button>
                    </div>
                {/if}
            {/if}
        </form>
    </Card>

    {#if searched}
        <div class="mb-6 flex items-center justify-between">
            <p class="text-slate-600 dark:text-slate-400">
                {photosStore.totalElements} photos found
            </p>
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
    {:else}
        <div class="flex flex-col items-center justify-center py-20 text-center">
            <div class="mb-4 rounded-full bg-slate-100 p-6 dark:bg-slate-800">
                <Search class="h-12 w-12 text-slate-400" />
            </div>
            <h3 class="mb-2 text-lg font-semibold text-slate-900 dark:text-white">
                Start your search
            </h3>
            <p class="max-w-md text-slate-500 dark:text-slate-400">
                Enter hashtags or use filters to find photos. Try searching for popular tags like nature, portrait, or landscape.
            </p>
        </div>
    {/if}
</div>

{#if selectedPhoto}
    <PhotoModal photo={selectedPhoto} onclose={() => (selectedPhoto = null)} />
{/if}