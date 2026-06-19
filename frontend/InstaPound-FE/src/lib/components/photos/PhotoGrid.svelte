<script lang="ts">
    import type { Photo } from '$lib/types';
    import PhotoCard from './PhotoCard.svelte';
    import Spinner from '$lib/components/ui/Spinner.svelte';

    interface Props {
        photos: Photo[];
        loading?: boolean;
        onPhotoClick?: (photo: Photo) => void;
    }
    
    let { photos, loading = false, onPhotoClick }: Props = $props();
</script>
{#if loading}
    <div class="flex items-center justify-center py-20">
        <Spinner size="lg" />
    </div>
{:else if photos.length === 0}
    <div class="flex flex-col items-center justify-center py-20 text-center">
        <div class="mb-4 rounded-full bg-slate-100 p-6 dark:bg-slate-800">
            <svg class="h-12 w-12 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="1.5"
                        d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
            </svg>
        </div>
        <h3 class="mb-2 text-lg font-semibold text-slate-900 dark:text-white">No photos found</h3>
        <p class="text-slate-500 dark:text-slate-400">There are no photos to display at the moment.</p>
    </div>
{:else}
    <div class="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {#each photos as photo (photo.id)}
            <PhotoCard {photo} onclick={() => onPhotoClick?.(photo)} />
        {/each}
    </div>
{/if}