<script lang="ts">
    import { page } from '$app/state';
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import {ArrowLeft, Download, Edit, Trash2, Eye, Calendar, User, FileImage, Share2 } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import Modal from '$lib/components/ui/Modal.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Textarea from '$lib/components/ui/Textarea.svelte';
    import Select from '$lib/components/ui/Select.svelte';
    import Hashtag from "$lib/components/ui/Hashtag.svelte";
    import Spinner from '$lib/components/ui/Spinner.svelte';
    import { photosStore } from '$lib/stores/photo.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import type { ImageFormat, ImageFilter } from '$lib/types';
    let loading = $state(true);
    let error = $state('');
    let showEditModal = $state(false);
    let showDeleteModal = $state(false);
    let showDownloadModal = $state(false);
    let actionLoading = $state(false);

    let editDescription = $state('');
    let editHashtags = $state<string[]>([]);
    let hashtagInput = $state('');

    let downloadFormat = $state<ImageFormat | ''>('');
    let downloadWidth = $state<number | ''>('');
    let downloadHeight = $state<number | ''>('');
    let selectedFilters = $state<ImageFilter[]>([]);

    const formatOptions = [
        { value: '', label: 'Original Format' },
        { value: 'JPEG', label: 'JPEG' },
        { value: 'PNG', label: 'PNG' },
        { value: 'BMP', label: 'BMP' }
    ];
    const filterOptions: ImageFilter[] = ['GRAYSCALE', 'SEPIA', 'INVERT', 'BLUR', 'SHARPEN', 'VINTAGE'];

    onMount(async () => {
        const id = parseInt(page.params.id);
        if (isNaN(id)) {
            error = 'Invalid photo ID';
            loading = false;
            return;
        }
        await photosStore.getById(id);
        if (photosStore.currentPhoto) {
            editDescription = photosStore.currentPhoto.description || '';
            editHashtags = [...(photosStore.currentPhoto.hashtags || [])];
        }
        loading = false;
    });

    function formatDate(dateString: string): string {
        return new Date(dateString).toLocaleString('en-US', {
            weekday: 'long',
            month: 'long',
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

    function addHashtag() {
        const tag = hashtagInput.trim().toLowerCase().replace(/^#/, '');
        if (tag && !editHashtags.includes(tag)) {
            editHashtags = [...editHashtags, tag];
        }
        hashtagInput = '';
    }

    function removeHashtag(tag: string) {
        editHashtags = editHashtags.filter((t) => t !== tag);
    }

    function handleHashtagKeydown(e: KeyboardEvent) {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault();
            addHashtag();
        }
    }

    async function handleEdit() {
        if (!photosStore.currentPhoto) return;
        actionLoading = true;
        try {
            await photosStore.edit(photosStore.currentPhoto.id, {
                description: editDescription || undefined,
                hashtags: editHashtags
            });
            showEditModal = false;
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to update photo';
        } finally {
            actionLoading = false;
        }
    }

    async function handleDelete() {
        if (!photosStore.currentPhoto) return;
        actionLoading = true;
        try {
            await photosStore.deletePhoto(photosStore.currentPhoto.id);
            goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to delete photo';
            showDeleteModal = false;
        } finally {
            actionLoading = false;
        }
    }

    async function handleDownload(original: boolean) {
        if (!photosStore.currentPhoto) return;
        actionLoading = true;
        try {
            await photosStore.download(photosStore.currentPhoto.id, {
                original,
                format: downloadFormat || undefined,
                width: downloadWidth || undefined,
                height: downloadHeight || undefined,
                filters: selectedFilters.length > 0 ? selectedFilters : undefined
            });
            showDownloadModal = false;
        } catch (err) {
            error = err instanceof Error ? err.message : 'Download failed';
        } finally {
            actionLoading = false;
        }
    }

    function toggleFilter(filter: ImageFilter) {
        if (selectedFilters.includes(filter)) {
            selectedFilters = selectedFilters.filter((f) => f !== filter);
        } else {
            selectedFilters = [...selectedFilters, filter];
        }
    }

    const canEdit = $derived(
        authStore.isAuthenticated &&
        photosStore.currentPhoto &&
        (authStore.user?.id === photosStore.currentPhoto.authorId || authStore.isAdmin)
    );
</script>

<svelte:head>
    <title>{photosStore.currentPhoto?.originalFilename || 'Photo'} - InstaPound</title>
</svelte:head>

{#if loading}
    <div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <Spinner size="lg" />
    </div>
{:else if photosStore.error || !photosStore.currentPhoto}
    <div class="mx-auto max-w-2xl px-4 py-20 text-center">
        <Card variant="elevated" padding="lg">
            <FileImage class="mx-auto mb-4 h-16 w-16 text-slate-300" />
            <h1 class="mb-2 text-2xl font-bold text-slate-900 dark:text-white">Photo not found</h1>
            <p class="mb-6 text-slate-600 dark:text-slate-400">
                {photosStore.error || 'The photo you\'re looking for doesn\'t exist or has been removed.'}
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
    {@const photo = photosStore.currentPhoto}
    <div class="mx-auto max-w-7xl px-4 py-8">
        <button
                onclick={() => history.back()}
                class="mb-6 flex items-center gap-2 text-sm font-medium text-slate-600 transition-colors hover:text-slate-900 dark:text-slate-400 dark:hover:text-white"
        >
            <ArrowLeft class="h-4 w-4" />
            Go back
        </button>
        {#if error}
            <Alert variant="error" class="mb-6" dismissible>
                {error}
            </Alert>
        {/if}
        <div class="grid gap-8 lg:grid-cols-3">
            <div class="lg:col-span-2">
                <Card variant="elevated" padding="none" class="overflow-hidden">
                    <div class="bg-slate-100 dark:bg-slate-800">
                        <img
                                src={photo.imageUrl}
                                alt={photo.description || photo.originalFilename}
                                class="h-auto w-full"
                        />
                    </div>
                </Card>
            </div>

            <div class="space-y-6">
                <Card variant="elevated" padding="lg">
                    <a
                            href="/user/{photo.authorId}"
                            class="flex items-center gap-4 transition-opacity hover:opacity-80"
                    >
                        <div class="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-xl font-bold text-white">
                            {photo.author.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <p class="font-semibold text-slate-900 dark:text-white">{photo.author}</p>
                            <p class="text-sm text-slate-500">View profile</p>
                        </div>
                    </a>
                </Card>

                {#if photo.description}
                    <Card variant="elevated" padding="lg">
                        <h3 class="mb-2 text-sm font-medium text-slate-500">Description</h3>
                        <p class="text-slate-700 dark:text-slate-300">{photo.description}</p>
                    </Card>
                {/if}

                {#if photo.hashtags && photo.hashtags.length > 0}
                    <Card variant="elevated" padding="lg">
                        <h3 class="mb-3 text-sm font-medium text-slate-500">Hashtags</h3>
                        <div class="flex flex-wrap gap-2">
                            {#each photo.hashtags as tag}
                                <a href="/search?tag={tag}">
                                    <Hashtag {tag} />
                                </a>
                            {/each}
                        </div>
                    </Card>
                {/if}

                <Card variant="elevated" padding="lg">
                    <div class="grid grid-cols-2 gap-4">
                        <div class="rounded-xl bg-slate-50 p-4 text-center dark:bg-slate-800">
                            <Eye class="mx-auto mb-2 h-5 w-5 text-slate-400" />
                            <p class="text-2xl font-bold text-slate-900 dark:text-white">{photo.viewCount}</p>
                            <p class="text-sm text-slate-500">Views</p>
                        </div>
                        <div class="rounded-xl bg-slate-50 p-4 text-center dark:bg-slate-800">
                            <Download class="mx-auto mb-2 h-5 w-5 text-slate-400" />
                            <p class="text-2xl font-bold text-slate-900 dark:text-white">{photo.downloadCount}</p>
                            <p class="text-sm text-slate-500">Downloads</p>
                        </div>
                    </div>
                </Card>

                <Card variant="elevated" padding="lg">
                    <h3 class="mb-4 text-sm font-medium text-slate-500">File Information</h3>
                    <div class="space-y-3 text-sm">
                        <div class="flex justify-between">
                            <span class="text-slate-500">Filename</span>
                            <span
                                    class="max-w-[200px] truncate font-medium text-slate-900 dark:text-white">{photo.originalFilename}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-slate-500">Dimensions</span>
                            <span class="font-medium text-slate-900 dark:text-white">{photo.width} × {photo.height}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-slate-500">File Size</span>
                            <span class="font-medium text-slate-900 dark:text-white">{formatSize(photo.fileSizeBytes)}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-slate-500">Uploaded</span>
                            <span class="font-medium text-slate-900 dark:text-white">{formatDate(photo.uploadedAt)}</span>
                        </div>
                        {#if photo.editedAt}
                            <div class="flex justify-between">
                                <span class="text-slate-500">Edited</span>
                                <span class="font-medium text-slate-900 dark:text-white">{formatDate(photo.editedAt)}</span>
                            </div>
                        {/if}
                    </div>
                </Card>

                <div class="space-y-3">
                    <Button class="w-full" onclick={() => (showDownloadModal = true)}>
                        <Download class="h-4 w-4" />
                        Download
                    </Button>
                    {#if canEdit}
                        <div class="flex gap-3">
                            <Button variant="outline" class="flex-1" onclick={() => (showEditModal = true)}>
                                <Edit class="h-4 w-4" />
                                Edit
                            </Button>
                            <Button variant="danger" class="flex-1" onclick={() => (showDeleteModal = true)}>
                                <Trash2 class="h-4 w-4" />
                                Delete
                            </Button>
                        </div>
                    {/if}
                </div>
            </div>
        </div>
    </div>

    <Modal bind:open={showEditModal} title="Edit Photo" size="md">
        <div class="space-y-4">
			<Textarea
                    label="Description"
                    bind:value={editDescription}
                    placeholder="Add a description..."
                    rows={3}
            />
            <div>
                <label class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">Hashtags</label>
                <div class="flex gap-2">
                    <Input
                            type="text"
                            placeholder="Add hashtag..."
                            bind:value={hashtagInput}
                            onkeydown={handleHashtagKeydown}
                            class="flex-1"
                    />
                    <Button type="button" variant="secondary" onclick={addHashtag}>Add</Button>
                </div>
                {#if editHashtags.length > 0}
                    <div class="mt-3 flex flex-wrap gap-2">
                        {#each editHashtags as tag}
                            <Hashtag {tag} removable onremove={() => removeHashtag(tag)} />
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
        <div class="mt-6 flex gap-3">
            <Button variant="secondary" class="flex-1" onclick={() => (showEditModal = false)}>Cancel</Button>
            <Button class="flex-1" loading={actionLoading} onclick={handleEdit}>Save Changes</Button>
        </div>
    </Modal>

    <Modal bind:open={showDeleteModal} title="Delete Photo" size="sm">
        <p class="mb-6 text-slate-600 dark:text-slate-400">
            Are you sure you want to delete this photo? This action cannot be undone.
        </p>
        <div class="flex gap-3">
            <Button variant="secondary" class="flex-1" onclick={() => (showDeleteModal = false)}>Cancel</Button>
            <Button variant="danger" class="flex-1" loading={actionLoading} onclick={handleDelete}>Delete</Button>
        </div>
    </Modal>

    <Modal bind:open={showDownloadModal} title="Download Options" size="md">
        <div class="space-y-4">
            <Select
                    bind:value={downloadFormat}
                    options={formatOptions}
                    label="Format"
                    placeholder="Choose format"
            />
            <div class="grid grid-cols-2 gap-3">
                <Input type="number" bind:value={downloadWidth} label="Width (px)" placeholder="Auto" />
                <Input type="number" bind:value={downloadHeight} label="Height (px)" placeholder="Auto" />
            </div>
            <div>
                <label class="mb-2 block text-sm font-medium text-slate-700 dark:text-slate-300">Filters</label>
                <div class="flex flex-wrap gap-2">
                    {#each filterOptions as filter}
                        <button
                                onclick={() => toggleFilter(filter)}
                                class="rounded-lg px-3 py-1.5 text-sm transition-colors
							{selectedFilters.includes(filter)
								? 'bg-violet-600 text-white'
								: 'bg-slate-100 text-slate-700 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700'}"
                        >
                            {filter}
                        </button>
                    {/each}
                </div>
            </div>
        </div>
        <div class="mt-6 space-y-3">
            <Button class="w-full" loading={actionLoading} onclick={() => handleDownload(true)}>
                <Download class="h-4 w-4" />
                Download Original
            </Button>
            {#if downloadFormat || downloadWidth || downloadHeight || selectedFilters.length > 0}
                <Button variant="secondary" class="w-full" loading={actionLoading} onclick={() => handleDownload(false)}>
                    <FileImage class="h-4 w-4" />
                    Download Processed
                </Button>
            {/if}
        </div>
    </Modal>
{/if}