<script lang="ts">
    import type { Photo, ImageFormat, ImageFilter } from '$lib/types';
    import { X, Download, Edit, Trash2, Eye, Calendar, User, FileImage, Maximize } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Hashtag from "$lib/components/ui/Hashtag.svelte";
    import Select from '$lib/components/ui/Select.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import { photosStore } from '$lib/stores/photo.svelte';
    import { authStore } from '$lib/stores/auth.svelte';

    interface Props {
        photo: Photo;
        onclose: () => void;
        onedit?: () => void;
        ondelete?: () => void;
    }

    let { photo, onclose, onedit, ondelete }: Props = $props();
    let showDownloadOptions = $state(false);
    let downloadFormat = $state<ImageFormat | ''>('');
    let downloadWidth = $state<number | ''>('');
    let downloadHeight = $state<number | ''>('');
    let selectedFilters = $state<ImageFilter[]>([]);
    let downloading = $state(false);

    const formatOptions = [
        { value: '', label: 'Original Format' },
        { value: 'JPEG', label: 'JPEG' },
        { value: 'PNG', label: 'PNG' },
        { value: 'BMP', label: 'BMP' }
    ];
    const filterOptions: ImageFilter[] = ['GRAYSCALE', 'SEPIA', 'INVERT', 'BLUR', 'SHARPEN', 'VINTAGE'];

    function formatDate(dateString: string): string {
        return new Date(dateString).toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: 'numeric',
            minute: '2-digit'
        });
    }

    function formatSize(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    async function handleDownload(original: boolean = true) {
        downloading = true;
        try {
            await photosStore.download(photo.id, {
                original,
                format: downloadFormat || undefined,
                width: downloadWidth || undefined,
                height: downloadHeight || undefined,
                filters: selectedFilters.length > 0 ? selectedFilters : undefined
            });
        } catch {
            // Error handled in store
        } finally {
            downloading = false;
        }
    }

    function toggleFilter(filter: ImageFilter) {
        if (selectedFilters.includes(filter)) {
            selectedFilters = selectedFilters.filter((f) => f !== filter);
        } else {
            selectedFilters = [...selectedFilters, filter];
        }
    }

    const canEdit = $derived(authStore.isAuthenticated && (authStore.user?.id === photo.authorId || authStore.isAdmin));
</script>
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm"
        onclick={(e) => e.target === e.currentTarget && onclose()}
>
    <div class="relative flex max-h-[90vh] w-full max-w-6xl flex-col overflow-hidden rounded-2xl bg-white shadow-2xl dark:bg-slate-900 lg:flex-row">
        <button
                onclick={onclose}
                class="absolute right-4 top-4 z-10 rounded-full bg-black/50 p-2 text-white transition-colors hover:bg-black/70"
        >
            <X class="h-5 w-5" />
        </button>

        <div class="relative flex flex-1 items-center justify-center bg-slate-100 dark:bg-slate-800">
            <img
                    src={photo.imageUrl}
                    alt={photo.description || photo.originalFilename}
                    class="max-h-[50vh] w-full object-contain lg:max-h-[85vh]"
            />
        </div>

        <div class="w-full overflow-y-auto p-6 lg:w-96">
            <div class="mb-6 flex items-center gap-3">
                <div class="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-lg font-semibold text-white">
                    {photo.author.charAt(0).toUpperCase()}
                </div>
                <div>
                    <a
                            href="/user/{photo.authorId}"
                            class="font-semibold text-slate-900 transition-colors hover:text-violet-600 dark:text-white dark:hover:text-violet-400"
                    >
                        {photo.author}
                    </a>
                    <p class="flex items-center gap-1.5 text-sm text-slate-500">
                        <Calendar class="h-4 w-4" />
                        {formatDate(photo.uploadedAt)}
                    </p>
                </div>
            </div>

            {#if photo.description}
                <div class="mb-6">
                    <h3 class="mb-2 text-sm font-medium text-slate-500 dark:text-slate-400">Description</h3>
                    <p class="text-slate-700 dark:text-slate-300">{photo.description}</p>
                </div>
            {/if}

            {#if photo.hashtags && photo.hashtags.length > 0}
                <div class="mb-6">
                    <h3 class="mb-2 text-sm font-medium text-slate-500 dark:text-slate-400">Hashtags</h3>
                    <div class="flex flex-wrap gap-2">
                        {#each photo.hashtags as tag}
                            <Hashtag {tag} />
                        {/each}
                    </div>
                </div>
            {/if}

            <div class="mb-6 grid grid-cols-2 gap-4">
                <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                    <div class="flex items-center gap-2 text-slate-500">
                        <Eye class="h-4 w-4" />
                        <span class="text-sm">Views</span>
                    </div>
                    <p class="mt-1 text-2xl font-bold text-slate-900 dark:text-white">{photo.viewCount}</p>
                </div>
                <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                    <div class="flex items-center gap-2 text-slate-500">
                        <Download class="h-4 w-4" />
                        <span class="text-sm">Downloads</span>
                    </div>
                    <p class="mt-1 text-2xl font-bold text-slate-900 dark:text-white">{photo.downloadCount}</p>
                </div>
            </div>

            <div class="mb-6 rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                <h3 class="mb-3 text-sm font-medium text-slate-500 dark:text-slate-400">File Info</h3>
                <div class="space-y-2 text-sm">
                    <div class="flex justify-between">
                        <span class="text-slate-500">Dimensions</span>
                        <span class="font-medium text-slate-900 dark:text-white">{photo.width} × {photo.height}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-slate-500">Size</span>
                        <span class="font-medium text-slate-900 dark:text-white">{formatSize(photo.fileSizeBytes)}</span>
                    </div>
                    <div class="flex justify-between">
                        <span class="text-slate-500">Filename</span>
                        <span
                                class="max-w-[150px] truncate font-medium text-slate-900 dark:text-white">{photo.originalFilename}</span>
                    </div>
                </div>
            </div>

            <div class="mb-6">
                <button
                        onclick={() => (showDownloadOptions = !showDownloadOptions)}
                        class="mb-3 flex w-full items-center justify-between text-sm font-medium text-slate-700 dark:text-slate-300"
                >
                    <span>Download Options</span>
                    <svg
                            class="h-5 w-5 transition-transform {showDownloadOptions ? 'rotate-180' : ''}"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                    >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                    </svg>
                </button>
                {#if showDownloadOptions}
                    <div class="space-y-4 rounded-xl bg-slate-50 p-4 dark:bg-slate-800">
                        <Select
                                bind:value={downloadFormat}
                                options={formatOptions}
                                label="Format"
                                placeholder="Choose format"
                        />
                        <div class="grid grid-cols-2 gap-3">
                            <Input
                                    type="number"
                                    bind:value={downloadWidth}
                                    label="Width"
                                    placeholder="Auto"
                            />
                            <Input
                                    type="number"
                                    bind:value={downloadHeight}
                                    label="Height"
                                    placeholder="Auto"
                            />
                        </div>
                        <fieldset>
                            <legend class="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300">Filters</legend>
                            <div class="flex flex-wrap gap-2">
                                {#each filterOptions as filter}
                                    <button
                                            onclick={() => toggleFilter(filter)}
                                            class="rounded-lg px-3 py-1.5 text-sm transition-colors
										{selectedFilters.includes(filter)
											? 'bg-violet-600 text-white'
											: 'bg-white text-slate-700 hover:bg-slate-100 dark:bg-slate-700 dark:text-slate-300 dark:hover:bg-slate-600'}"
                                    >
                                        {filter}
                                    </button>
                                {/each}
                            </div>
                        </fieldset>
                    </div>
                {/if}
            </div>

            <div class="space-y-3">
                <Button class="w-full" loading={downloading} onclick={() => handleDownload(true)}>
                    <Download class="h-4 w-4" />
                    Download Original
                </Button>
                {#if showDownloadOptions && (downloadFormat || downloadWidth || downloadHeight || selectedFilters.length > 0)}
                    <Button
                            variant="secondary"
                            class="w-full"
                            loading={downloading}
                            onclick={() => handleDownload(false)}
                    >
                        <FileImage class="h-4 w-4" />
                        Download Processed
                    </Button>
                {/if}
                {#if canEdit}
                    <div class="flex gap-3">
                        <Button variant="outline" class="flex-1" onclick={onedit}>
                            <Edit class="h-4 w-4" />
                            Edit
                        </Button>
                        <Button variant="danger" class="flex-1" onclick={ondelete}>
                            <Trash2 class="h-4 w-4" />
                            Delete
                        </Button>
                    </div>
                {/if}
            </div>
        </div>
    </div>
</div>