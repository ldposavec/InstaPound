<script lang="ts">
  import type { Photo, PhotoFilter, PaginatedResponse } from '../types';
  import { photoService } from '../services/api';
  import { onMount } from 'svelte';
  import PhotoCard from '../components/PhotoCard.svelte';
  import PhotoModal from '../components/PhotoModal.svelte';
  import DownloadModal from '../components/DownloadModal.svelte';
  import { ChevronLeft, ChevronRight, Loader2, Image } from 'lucide-svelte';
  import { toastStore } from '../stores/index.svelte';

  let photos = $state<Photo[]>([]);
  let isLoading = $state(true);
  let pagination = $state({
    page: 1,
    pageSize: 10,
    total: 0,
    totalPages: 0
  });
  
  let selectedPhoto = $state<Photo | null>(null);
  let showDownloadModal = $state(false);
  let downloadPhoto = $state<Photo | null>(null);

  async function loadPhotos(page: number = 1) {
    isLoading = true;
    
    try {
      const result = await photoService.getPhotos(page, pagination.pageSize);
      
      if (result.success && result.data) {
        photos = result.data.items;
        pagination = {
          page: result.data.page,
          pageSize: result.data.pageSize,
          total: result.data.total,
          totalPages: result.data.totalPages
        };
      }
    } finally {
      isLoading = false;
    }
  }

  function handleView(photo: Photo) {
    selectedPhoto = photo;
  }

  function handleDownload(photo: Photo) {
    downloadPhoto = photo;
    showDownloadModal = true;
  }

  async function handleDelete(photo: Photo) {
    if (!confirm('Are you sure you want to delete this photo?')) return;
    
    const result = await photoService.deletePhoto(photo.id);
    
    if (result.success) {
      await loadPhotos(pagination.page);
      if (selectedPhoto?.id === photo.id) {
        selectedPhoto = null;
      }
    } else {
      toastStore.error(result.error || 'Failed to delete photo');
    }
  }

  onMount(() => {
    loadPhotos();
  });
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
        Photo Gallery
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        Browse the latest {pagination.total} photos uploaded by our community
      </p>
    </div>

    {#if isLoading}
      <div class="flex items-center justify-center py-20">
        <Loader2 class="w-12 h-12 text-violet-600 animate-spin" />
      </div>
    {:else if photos.length === 0}
      <div class="flex flex-col items-center justify-center py-20 text-center">
        <div class="w-24 h-24 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mb-4">
          <Image class="w-12 h-12 text-gray-400" />
        </div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">No photos yet</h2>
        <p class="text-gray-600 dark:text-gray-400">Be the first to upload a photo!</p>
      </div>
    {:else}
      <!-- Photo Grid -->
      <div class="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {#each photos as photo (photo.id)}
          <PhotoCard
            {photo}
            onView={() => handleView(photo)}
            onDownload={() => handleDownload(photo)}
            onDelete={() => handleDelete(photo)}
          />
        {/each}
      </div>

      <!-- Pagination -->
      {#if pagination.totalPages > 1}
        <div class="flex items-center justify-center gap-2 mt-10">
          <button
            onclick={() => loadPhotos(pagination.page - 1)}
            disabled={pagination.page <= 1}
            class="flex items-center gap-1 px-4 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft class="w-5 h-5" />
            Previous
          </button>
          
          <div class="flex items-center gap-1">
            {#each Array.from({ length: Math.min(5, pagination.totalPages) }, (_, i) => {
              const startPage = Math.max(1, Math.min(pagination.page - 2, pagination.totalPages - 4));
              return startPage + i;
            }) as pageNum}
              <button
                onclick={() => loadPhotos(pageNum)}
                class="w-10 h-10 rounded-lg font-medium transition-colors {pageNum === pagination.page 
                  ? 'bg-violet-600 text-white' 
                  : 'bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700'}"
              >
                {pageNum}
              </button>
            {/each}
          </div>
          
          <button
            onclick={() => loadPhotos(pagination.page + 1)}
            disabled={pagination.page >= pagination.totalPages}
            class="flex items-center gap-1 px-4 py-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Next
            <ChevronRight class="w-5 h-5" />
          </button>
        </div>
      {/if}
    {/if}
  </div>
</div>

<!-- Photo Modal -->
{#if selectedPhoto}
  <PhotoModal
    photo={selectedPhoto}
    onClose={() => selectedPhoto = null}
    onDelete={() => {
      if (selectedPhoto) {
        handleDelete(selectedPhoto);
      }
    }}
  />
{/if}

<!-- Download Modal -->
{#if showDownloadModal && downloadPhoto}
  <DownloadModal
    photo={downloadPhoto}
    onClose={() => {
      showDownloadModal = false;
      downloadPhoto = null;
    }}
  />
{/if}
