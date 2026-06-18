<script lang="ts">
    import type { Photo } from '$lib/types';
    import Hashtag from "$lib/components/ui/Hashtag.svelte";
    import { Eye, Download, Calendar, User } from 'lucide-svelte';

    interface Props {
        photo: Photo;
        onclick?: () => void;
    }

    let { photo, onclick }: Props = $props();

    function formatDate(dateString: string): string {
        return new Date(dateString).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    function formatSize(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
</script>
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
        class="group cursor-pointer overflow-hidden rounded-2xl bg-white shadow-lg shadow-slate-200/50 transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-slate-300/50 dark:bg-slate-900 dark:shadow-slate-900/50 dark:hover:shadow-slate-800/50"
        onclick={onclick}
>
    <div class="relative aspect-square overflow-hidden bg-slate-100 dark:bg-slate-800">
        <img
                src={photo.thumbnailUrl || photo.imageUrl}
                alt={photo.description || photo.originalFilename}
                class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
                loading="lazy"
        />

        <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100">
            <div class="absolute bottom-0 left-0 right-0 p-4">
                <div class="flex items-center gap-4 text-white">
					<span class="flex items-center gap-1 text-sm">
						<Eye class="h-4 w-4" />
                        {photo.viewCount}
					</span>
                    <span class="flex items-center gap-1 text-sm">
						<Download class="h-4 w-4" />
                        {photo.downloadCount}
					</span>
                </div>
            </div>
        </div>
    </div>

    <div class="p-4">
        <div class="mb-2 flex items-center justify-between text-sm text-slate-500 dark:text-slate-400">
            <a
                    href="/user/{photo.authorId}"
                    class="flex items-center gap-1.5 transition-colors hover:text-violet-600 dark:hover:text-violet-400"
                    onclick={(e) => e.stopPropagation()}
            >
                <User class="h-4 w-4" />
                {photo.author}
            </a>
            <span class="flex items-center gap-1.5">
				<Calendar class="h-4 w-4" />
                {formatDate(photo.uploadedAt)}
			</span>
        </div>

        {#if photo.description}
            <p class="mb-3 line-clamp-2 text-sm text-slate-700 dark:text-slate-300">
                {photo.description}
            </p>
        {/if}

        {#if photo.hashtags && photo.hashtags.length > 0}
            <div class="flex flex-wrap gap-1.5">
                {#each photo.hashtags.slice(0, 3) as tag}
                    <Hashtag {tag} />
                {/each}
                {#if photo.hashtags.length > 3}
					<span class="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-500 dark:bg-slate-800 dark:text-slate-400">
						+{photo.hashtags.length - 3}
					</span>
                {/if}
            </div>
        {/if}

        <div class="mt-3 flex items-center justify-between border-t border-slate-100 pt-3 text-xs text-slate-400 dark:border-slate-800 dark:text-slate-500">
            <span>{photo.width} × {photo.height}</span>
            <span>{formatSize(photo.fileSizeBytes)}</span>
        </div>
    </div>
</div>