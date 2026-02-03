<script lang="ts">
    import { Upload, X, Image, Check, AlertCircle } from 'lucide-svelte';
    import Button from '$lib/components/ui/Button.svelte';
    import Input from '$lib/components/ui/Input.svelte';
    import Textarea from '$lib/components/ui/Textarea.svelte';
    import Select from '$lib/components/ui/Select.svelte';
    import Alert from '$lib/components/ui/Alert.svelte';
    import Card from '$lib/components/ui/Card.svelte';
    import Hashtag from "$lib/components/ui/Hashtag.svelte";
    import { photosStore } from '$lib/stores/photo.svelte';
    import { authStore } from '$lib/stores/auth.svelte';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import type { ImageFormat, ImageFilter, StorageType } from '$lib/types';

    let file = $state<File | null>(null);
    let preview = $state<string | null>(null);
    let description = $state('');
    let hashtagInput = $state('');
    let hashtags = $state<string[]>([]);
    let format = $state<ImageFormat | ''>('');
    let width = $state<number | ''>('');
    let height = $state<number | ''>('');
    let storageType = $state<StorageType>('LOCAL');
    let selectedFilters = $state<ImageFilter[]>([]);
    let loading = $state(false);
    let error = $state('');
    let success = $state(false);
    let dragActive = $state(false);

    const formatOptions = [
        { value: '', label: 'Keep Original Format' },
        { value: 'JPEG', label: 'JPEG' },
        { value: 'PNG', label: 'PNG' },
        { value: 'BMP', label: 'BMP' }
    ];

    const storageOptions = [
        { value: 'LOCAL', label: 'Local Storage' },
        { value: 'CLOUDINARY', label: 'Cloud Storage (Cloudinary)' }
    ];

    const filterOptions: { value: ImageFilter; label: string }[] = [
        { value: 'GRAYSCALE', label: 'Grayscale' },
        { value: 'SEPIA', label: 'Sepia' },
        { value: 'INVERT', label: 'Invert' },
        { value: 'BLUR', label: 'Blur' },
        { value: 'SHARPEN', label: 'Sharpen' },
        { value: 'VINTAGE', label: 'Vintage' }
    ];

    function toggleFilter(filter: ImageFilter) {
        if (selectedFilters.includes(filter)) {
            selectedFilters = selectedFilters.filter(f => f !== filter);
        } else {
            selectedFilters = [...selectedFilters, filter];
        }
    }

    onMount(() => {
        if (!authStore.isAuthenticated) {
            goto('/login');
        }
    });

    function handleFileSelect(e: Event) {
        const target = e.target as HTMLInputElement;
        if (target.files && target.files[0]) {
            selectFile(target.files[0]);
        }
    }

    function handleDrop(e: DragEvent) {
        e.preventDefault();
        dragActive = false;
        if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
            selectFile(e.dataTransfer.files[0]);
        }
    }

    function handleDragOver(e: DragEvent) {
        e.preventDefault();
        dragActive = true;
    }

    function handleDragLeave() {
        dragActive = false;
    }

    function selectFile(selectedFile: File) {
        if (!selectedFile.type.startsWith('image/')) {
            error = 'Please select an image file';
            return;
        }
        file = selectedFile;
        const reader = new FileReader();
        reader.onload = (e) => {
            preview = e.target?.result as string;
        };
        reader.readAsDataURL(selectedFile);
        error = '';
    }

    function clearFile() {
        file = null;
        preview = null;
    }

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

    async function handleSubmit(e: Event) {
        e.preventDefault();
        if (!file) {
            error = 'Please select a file to upload';
            return;
        }
        loading = true;
        error = '';
        success = false;
        try {
            const photo = await photosStore.upload(file, {
                description: description || undefined,
                hashtags: hashtags.length > 0 ? hashtags : undefined,
                storageType,
                format: format || undefined,
                width: width || undefined,
                height: height || undefined,
                filters: selectedFilters.length > 0 ? selectedFilters : undefined
            });
            success = true;
            setTimeout(() => {
                goto(`/photo/${photo.id}`);
            }, 1500);
        } catch (err) {
            error = err instanceof Error ? err.message : 'Upload failed. Please try again.';
        } finally {
            loading = false;
        }
    }
</script>

<svelte:head>
    <title>Upload Photo - InstaPound</title>
</svelte:head>

<div class="mx-auto max-w-4xl px-4 py-12">
    <div class="mb-8 text-center">
        <h1 class="text-3xl font-bold text-slate-900 dark:text-white">Upload Photo</h1>
        <p class="mt-2 text-slate-600 dark:text-slate-400">Share your amazing photos with the world</p>
    </div>
    {#if success}
        <Alert variant="success" class="mb-6">
            Photo uploaded successfully! Redirecting...
        </Alert>
    {/if}
    {#if error}
        <Alert variant="error" class="mb-6" dismissible>
            {error}
        </Alert>
    {/if}
    <form onsubmit={handleSubmit}>
        <div class="grid gap-8 lg:grid-cols-2">
            <Card variant="elevated" padding="lg">
                <h2 class="mb-4 text-lg font-semibold text-slate-900 dark:text-white">Select Image</h2>
                {#if !file}
                    <!-- svelte-ignore a11y_no_static_element_interactions -->
                    <label
                            class="flex min-h-[300px] cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed transition-colors
						{dragActive
							? 'border-violet-500 bg-violet-50 dark:border-violet-400 dark:bg-violet-950/50'
							: 'border-slate-300 hover:border-violet-400 dark:border-slate-700 dark:hover:border-violet-500'}"
                            ondragover={handleDragOver}
                            ondragleave={handleDragLeave}
                            ondrop={handleDrop}
                    >
                        <input type="file" accept="image/*" class="hidden" onchange={handleFileSelect} />
                        <div class="mb-4 rounded-full bg-violet-100 p-4 dark:bg-violet-900/50">
                            <Upload class="h-8 w-8 text-violet-600 dark:text-violet-400" />
                        </div>
                        <p class="mb-2 text-lg font-medium text-slate-900 dark:text-white">
                            Drag & drop or click to upload
                        </p>
                        <p class="text-sm text-slate-500">PNG, JPG, BMP up to 50MB</p>
                    </label>
                {:else}
                    <div class="relative overflow-hidden rounded-xl bg-slate-100 dark:bg-slate-800">
                        <img src={preview} alt="Preview" class="h-auto w-full object-contain" style="max-height: 400px;" />
                        <button
                                type="button"
                                onclick={clearFile}
                                class="absolute right-2 top-2 rounded-full bg-black/50 p-2 text-white transition-colors hover:bg-black/70"
                        >
                            <X class="h-5 w-5" />
                        </button>
                    </div>
                    <div class="mt-4 flex items-center gap-4 text-sm text-slate-600 dark:text-slate-400">
						<span class="flex items-center gap-1.5">
							<Image class="h-4 w-4" />
                            {file.name}
						</span>
                        <span>
							{(file.size / (1024 * 1024)).toFixed(2)} MB
						</span>
                    </div>
                {/if}
            </Card>

            <div class="space-y-6">
                <Card variant="elevated" padding="lg">
                    <h2 class="mb-4 text-lg font-semibold text-slate-900 dark:text-white">Photo Details</h2>

                    <div class="space-y-5">
						<Textarea
                                label="Description"
                                placeholder="Add a description for your photo..."
                                bind:value={description}
                                rows={3}
                        />
                        <div>
                            <label class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                                Hashtags
                            </label>
                            <div class="flex gap-2">
                                <Input
                                        type="text"
                                        placeholder="Add hashtags..."
                                        bind:value={hashtagInput}
                                        onkeydown={handleHashtagKeydown}
                                        class="flex-1"
                                />
                                <Button type="button" variant="secondary" onclick={addHashtag}>Add</Button>
                            </div>
                            {#if hashtags.length > 0}
                                <div class="mt-3 flex flex-wrap gap-2">
                                    {#each hashtags as tag}
                                        <Hashtag {tag} removable onremove={() => removeHashtag(tag)} />
                                    {/each}
                                </div>
                            {/if}
                        </div>
                    </div>
                </Card>
                <Card variant="elevated" padding="lg">
                    <h2 class="mb-4 text-lg font-semibold text-slate-900 dark:text-white">Processing Options</h2>

                    <div class="space-y-5">
                        <Select
                                bind:value={format}
                                options={formatOptions}
                                label="Output Format"
                                placeholder="Keep original"
                        />
                        <div class="grid grid-cols-2 gap-4">
                            <Input
                                    type="number"
                                    label="Width (px)"
                                    placeholder="Auto"
                                    bind:value={width}
                            />
                            <Input
                                    type="number"
                                    label="Height (px)"
                                    placeholder="Auto"
                                    bind:value={height}
                            />
                        </div>
                        <div>
                            <label class="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">
                                Image Filters
                            </label>
                            <div class="grid grid-cols-2 gap-2 sm:grid-cols-3">
                                {#each filterOptions as option}
                                    <button
                                            type="button"
                                            onclick={() => toggleFilter(option.value)}
                                            class="rounded-lg border-2 px-3 py-2 text-sm font-medium transition-colors
                                                {selectedFilters.includes(option.value)
                                                    ? 'border-violet-500 bg-violet-50 text-violet-700 dark:border-violet-400 dark:bg-violet-950/50 dark:text-violet-300'
                                                    : 'border-slate-200 bg-white text-slate-700 hover:border-violet-300 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300 dark:hover:border-violet-600'}"
                                    >
                                        {option.label}
                                    </button>
                                {/each}
                            </div>
                            {#if selectedFilters.length > 0}
                                <p class="mt-2 text-xs text-slate-500 dark:text-slate-400">
                                    Selected: {selectedFilters.join(', ')}
                                </p>
                            {/if}
                        </div>
                        <Select
                                bind:value={storageType}
                                options={storageOptions}
                                label="Storage Location"
                        />
                    </div>
                </Card>
                <Button type="submit" class="w-full" size="lg" {loading} disabled={!file || success}>
                    {#if loading}
                        Uploading...
                    {:else if success}
                        <Check class="h-5 w-5" />
                        Uploaded!
                    {:else}
                        <Upload class="h-5 w-5" />
                        Upload Photo
                    {/if}
                </Button>
            </div>
        </div>
    </form>
</div>