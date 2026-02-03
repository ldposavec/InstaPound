<script lang="ts">
  import type { Photo } from '../types';
  import { X, Download, Edit, Trash2, Calendar, User, Hash, FileImage, Maximize } from 'lucide-svelte';
  import { authStore } from '../stores/index.svelte';
  import DownloadModal from './DownloadModal.svelte';

  interface Props {
    photo: Photo;
    onClose: () => void;
    onEdit?: () => void;
    onDelete?: () => void;
  }

  let { photo, onClose, onEdit, onDelete }: Props = $props();

  let showDownloadModal = $state(false);
  let imageLoaded = $state(false);

  function formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
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

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Escape') {
      onClose();
    }
  }
</script>

<svelte:window onkeydown={handleKeydown} />

<!-- Modal backdrop -->
<div 
  class="fixed inset-0 z-50 bg-black/80 flex items-center justify-center p-4"
  onclick={onClose}
  role="dialog"
  aria-modal="true"
>
  <!-- Modal content -->
  <div 
    class="bg-white dark:bg-gray-900 rounded-2xl max-w-6xl w-full max-h-[90vh] overflow-hidden flex flex-col md:flex-row"
    onclick={(e: MouseEvent) => e.stopPropagation()}
  >
    <!-- Image section -->
    <div class="flex-1 bg-gray-100 dark:bg-gray-800 relative min-h-[300px] md:min-h-[500px]">
      {#if !imageLoaded}
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="animate-spin w-12 h-12 border-4 border-violet-500 border-t-transparent rounded-full"></div>
        </div>
      {/if}
      
      <img
        src={photo.fullUrl}
        alt={photo.description}
        class="w-full h-full object-contain"
        class:opacity-0={!imageLoaded}
        class:opacity-100={imageLoaded}
        onload={() => imageLoaded = true}
      />

      <!-- Close button -->
      <button
        onclick={onClose}
        class="absolute top-4 right-4 p-2 bg-black/50 hover:bg-black/70 text-white rounded-full transition-colors"
        aria-label="Close"
      >
        <X class="w-6 h-6" />
      </button>
    </div>

    <!-- Info section -->
    <div class="w-full md:w-96 p-6 overflow-y-auto">
      <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-4">
        Photo Details
      </h2>

      <!-- Description -->
      <div class="mb-6">
        <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">
          Description
        </h3>
        <p class="text-gray-700 dark:text-gray-300">
          {photo.description || 'No description provided'}
        </p>
      </div>

      <!-- Metadata -->
      <div class="space-y-4 mb-6">
        <div class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
          <User class="w-5 h-5" />
          <div>
            <p class="text-xs text-gray-500 dark:text-gray-500">Author</p>
            <p class="text-gray-900 dark:text-white font-medium">{photo.authorUsername}</p>
          </div>
        </div>

        <div class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
          <Calendar class="w-5 h-5" />
          <div>
            <p class="text-xs text-gray-500 dark:text-gray-500">Uploaded</p>
            <p class="text-gray-900 dark:text-white font-medium">{formatDate(photo.uploadedAt)}</p>
          </div>
        </div>

        <div class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
          <FileImage class="w-5 h-5" />
          <div>
            <p class="text-xs text-gray-500 dark:text-gray-500">Size & Format</p>
            <p class="text-gray-900 dark:text-white font-medium">
              {formatSize(photo.sizeBytes)} • {photo.format}
            </p>
          </div>
        </div>

        <div class="flex items-center gap-3 text-gray-600 dark:text-gray-400">
          <Maximize class="w-5 h-5" />
          <div>
            <p class="text-xs text-gray-500 dark:text-gray-500">Dimensions</p>
            <p class="text-gray-900 dark:text-white font-medium">
              {photo.width} × {photo.height} px
            </p>
          </div>
        </div>
      </div>

      <!-- Hashtags -->
      {#if photo.hashtags.length > 0}
        <div class="mb-6">
          <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2 flex items-center gap-2">
            <Hash class="w-4 h-4" />
            Hashtags
          </h3>
          <div class="flex flex-wrap gap-2">
            {#each photo.hashtags as tag}
              <span class="px-3 py-1 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 text-sm rounded-full">
                #{tag}
              </span>
            {/each}
          </div>
        </div>
      {/if}

      <!-- Actions -->
      <div class="space-y-2 pt-4 border-t border-gray-200 dark:border-gray-700">
        <button
          onclick={() => showDownloadModal = true}
          class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-lg font-medium transition-colors"
        >
          <Download class="w-5 h-5" />
          Download Photo
        </button>

        {#if canEdit()}
          <div class="flex gap-2">
            {#if onEdit}
              <button
                onclick={onEdit}
                class="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-blue-100 dark:bg-blue-900/30 hover:bg-blue-200 dark:hover:bg-blue-900/50 text-blue-700 dark:text-blue-300 rounded-lg font-medium transition-colors"
              >
                <Edit class="w-5 h-5" />
                Edit
              </button>
            {/if}
            
            {#if onDelete}
              <button
                onclick={onDelete}
                class="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-red-100 dark:bg-red-900/30 hover:bg-red-200 dark:hover:bg-red-900/50 text-red-700 dark:text-red-300 rounded-lg font-medium transition-colors"
              >
                <Trash2 class="w-5 h-5" />
                Delete
              </button>
            {/if}
          </div>
        {/if}
      </div>
    </div>
  </div>
</div>

{#if showDownloadModal}
  <DownloadModal 
    {photo}
    onClose={() => showDownloadModal = false}
  />
{/if}
