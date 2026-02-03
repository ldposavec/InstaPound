<script lang="ts">
  import type { Photo } from '../types';
  import { Calendar, User, Hash, Eye, Download, Edit, Trash2 } from 'lucide-svelte';
  import { push } from 'svelte-spa-router';
  import { authStore } from '../stores/index.svelte';

  interface Props {
    photo: Photo;
    onView?: () => void;
    onDownload?: () => void;
    onEdit?: () => void;
    onDelete?: () => void;
    showActions?: boolean;
  }

  let { 
    photo, 
    onView, 
    onDownload, 
    onEdit, 
    onDelete, 
    showActions = true 
  }: Props = $props();

  let imageLoaded = $state(false);
  let imageError = $state(false);

  function formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  function formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  function canEdit(): boolean {
    if (!authStore.user) return false;
    if (authStore.isAdmin) return true;
    return photo.authorId === authStore.user.id;
  }

  function handleClick() {
    if (onView) {
      onView();
    } else {
      push(`/photo/${photo.id}`);
    }
  }
</script>

<div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden group">
  <!-- Image container -->
  <div class="relative aspect-[3/2] overflow-hidden bg-gray-100 dark:bg-gray-700">
    {#if !imageLoaded && !imageError}
      <div class="absolute inset-0 flex items-center justify-center">
        <div class="animate-pulse w-12 h-12 rounded-full bg-gray-300 dark:bg-gray-600"></div>
      </div>
    {/if}
    
    {#if imageError}
      <div class="absolute inset-0 flex items-center justify-center text-gray-400">
        <span>Failed to load</span>
      </div>
    {:else}
      <img
        src={photo.thumbnailUrl}
        alt={photo.description}
        class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
        class:opacity-0={!imageLoaded}
        class:opacity-100={imageLoaded}
        onload={() => imageLoaded = true}
        onerror={() => imageError = true}
      />
    {/if}
    
    <!-- Overlay on hover -->
    <div class="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
      <div class="absolute bottom-0 left-0 right-0 p-4">
        <button
          onclick={handleClick}
          class="w-full py-2 bg-white/90 hover:bg-white text-gray-900 rounded-lg font-medium flex items-center justify-center gap-2 transition-colors"
        >
          <Eye class="w-4 h-4" />
          View Photo
        </button>
      </div>
    </div>
  </div>

  <!-- Content -->
  <div class="p-4">
    <p class="text-gray-900 dark:text-white font-medium line-clamp-2 mb-2">
      {photo.description || 'No description'}
    </p>

    <!-- Metadata -->
    <div class="space-y-2 text-sm text-gray-600 dark:text-gray-400">
      <div class="flex items-center gap-2">
        <User class="w-4 h-4" />
        <span>{photo.authorUsername}</span>
      </div>
      
      <div class="flex items-center gap-2">
        <Calendar class="w-4 h-4" />
        <span>{formatDate(photo.uploadedAt)}</span>
      </div>
    </div>

    <!-- Hashtags -->
    {#if photo.hashtags.length > 0}
      <div class="flex flex-wrap gap-1 mt-3">
        {#each photo.hashtags.slice(0, 4) as tag}
          <span class="inline-flex items-center gap-1 px-2 py-1 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 text-xs rounded-full">
            <Hash class="w-3 h-3" />
            {tag}
          </span>
        {/each}
        {#if photo.hashtags.length > 4}
          <span class="px-2 py-1 text-gray-500 text-xs">
            +{photo.hashtags.length - 4} more
          </span>
        {/if}
      </div>
    {/if}

    <!-- Actions -->
    {#if showActions}
      <div class="flex items-center gap-2 mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
        <button
          onclick={(e: MouseEvent) => { e.stopPropagation(); if (onDownload) onDownload(); }}
          class="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
          title="Download"
        >
          <Download class="w-4 h-4" />
          <span class="hidden sm:inline">Download</span>
        </button>
        
        {#if canEdit() && onEdit}
          <button
            onclick={(e: MouseEvent) => { e.stopPropagation(); onEdit(); }}
            class="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-lg transition-colors"
            title="Edit"
          >
            <Edit class="w-4 h-4" />
            <span class="hidden sm:inline">Edit</span>
          </button>
        {/if}
        
        {#if canEdit() && onDelete}
          <button
            onclick={(e: MouseEvent) => { e.stopPropagation(); onDelete(); }}
            class="flex-1 flex items-center justify-center gap-2 px-3 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
            title="Delete"
          >
            <Trash2 class="w-4 h-4" />
            <span class="hidden sm:inline">Delete</span>
          </button>
        {/if}
      </div>
    {/if}
  </div>
</div>

<style>
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
</style>
