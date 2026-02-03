<script lang="ts">
  import type { Photo, PhotoFilter } from '../types';
  import { photoService } from '../services/api';
  import { onMount } from 'svelte';
  import PhotoCard from '../components/PhotoCard.svelte';
  import PhotoModal from '../components/PhotoModal.svelte';
  import DownloadModal from '../components/DownloadModal.svelte';
  import { Search as SearchIcon, X, Filter, Loader2, Image, Hash, User, Calendar } from 'lucide-svelte';
  import { toastStore } from '../stores/index.svelte';

  let photos = $state<Photo[]>([]);
  let isLoading = $state(false);
  let hasSearched = $state(false);
  let showFilters = $state(false);
  
  let filters = $state<PhotoFilter>({
    hashtags: [],
    sizeMin: undefined,
    sizeMax: undefined,
    uploadDateFrom: undefined,
    uploadDateTo: undefined,
    author: undefined
  });
  
  let hashtagInput = $state('');
  let selectedPhoto = $state<Photo | null>(null);
  let showDownloadModal = $state(false);
  let downloadPhoto = $state<Photo | null>(null);

  function addHashtag() {
    if (hashtagInput.trim() && !filters.hashtags?.includes(hashtagInput.trim())) {
      filters.hashtags = [...(filters.hashtags || []), hashtagInput.trim().toLowerCase().replace('#', '')];
      hashtagInput = '';
    }
  }

  function removeHashtag(tag: string) {
    filters.hashtags = filters.hashtags?.filter(t => t !== tag);
  }

  function clearFilters() {
    filters = {
      hashtags: [],
      sizeMin: undefined,
      sizeMax: undefined,
      uploadDateFrom: undefined,
      uploadDateTo: undefined,
      author: undefined
    };
    hashtagInput = '';
  }

  async function handleSearch() {
    isLoading = true;
    hasSearched = true;
    
    try {
      // Clean up empty filter values
      const cleanFilters: PhotoFilter = {};
      if (filters.hashtags && filters.hashtags.length > 0) {
        cleanFilters.hashtags = filters.hashtags;
      }
      if (filters.author) cleanFilters.author = filters.author;
      if (filters.sizeMin) cleanFilters.sizeMin = filters.sizeMin;
      if (filters.sizeMax) cleanFilters.sizeMax = filters.sizeMax;
      if (filters.uploadDateFrom) cleanFilters.uploadDateFrom = filters.uploadDateFrom;
      if (filters.uploadDateTo) cleanFilters.uploadDateTo = filters.uploadDateTo;
      
      const result = await photoService.getPhotos(1, 50, cleanFilters);
      
      if (result.success && result.data) {
        photos = result.data.items;
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
      photos = photos.filter(p => p.id !== photo.id);
      if (selectedPhoto?.id === photo.id) {
        selectedPhoto = null;
      }
    } else {
      toastStore.error(result.error || 'Failed to delete photo');
    }
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      if (hashtagInput) {
        addHashtag();
      } else {
        handleSearch();
      }
    }
  }
</script>

<div class="min-h-screen bg-gray-50 dark:bg-gray-900">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
        Search Photos
      </h1>
      <p class="text-gray-600 dark:text-gray-400">
        Find photos by hashtags, author, date, or size
      </p>
    </div>

    <!-- Search Box -->
    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm p-6 mb-8">
      <!-- Hashtag Input -->
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          <Hash class="w-4 h-4 inline mr-1" />
          Hashtags
        </label>
        <div class="flex gap-2">
          <div class="flex-1 relative">
            <input
              type="text"
              bind:value={hashtagInput}
              onkeydown={handleKeydown}
              placeholder="Type a hashtag and press Enter"
              class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent"
            />
          </div>
          <button
            onclick={addHashtag}
            class="px-6 py-3 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-xl font-medium hover:bg-violet-200 dark:hover:bg-violet-900/50 transition-colors"
          >
            Add
          </button>
        </div>
        
        <!-- Hashtag Tags -->
        {#if filters.hashtags && filters.hashtags.length > 0}
          <div class="flex flex-wrap gap-2 mt-3">
            {#each filters.hashtags as tag}
              <span class="inline-flex items-center gap-1 px-3 py-1 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-full">
                #{tag}
                <button onclick={() => removeHashtag(tag)} class="hover:text-violet-900 dark:hover:text-violet-100">
                  <X class="w-4 h-4" />
                </button>
              </span>
            {/each}
          </div>
        {/if}
      </div>

      <!-- Toggle Filters -->
      <button
        onclick={() => showFilters = !showFilters}
        class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-violet-600 dark:hover:text-violet-400 mb-4"
      >
        <Filter class="w-4 h-4" />
        {showFilters ? 'Hide' : 'Show'} Advanced Filters
      </button>

      <!-- Advanced Filters -->
      {#if showFilters}
        <div class="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
          <!-- Author -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              <User class="w-4 h-4 inline mr-1" />
              Author
            </label>
            <input
              type="text"
              bind:value={filters.author}
              placeholder="Username"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>

          <!-- Size Range -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Size Range (KB)
            </label>
            <div class="flex gap-2">
              <input
                type="number"
                bind:value={filters.sizeMin}
                placeholder="Min"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500"
              />
              <input
                type="number"
                bind:value={filters.sizeMax}
                placeholder="Max"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500"
              />
            </div>
          </div>

          <!-- Date From -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              <Calendar class="w-4 h-4 inline mr-1" />
              From Date
            </label>
            <input
              type="date"
              bind:value={filters.uploadDateFrom}
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>

          <!-- Date To -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              <Calendar class="w-4 h-4 inline mr-1" />
              To Date
            </label>
            <input
              type="date"
              bind:value={filters.uploadDateTo}
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>
        </div>
      {/if}

      <!-- Action Buttons -->
      <div class="flex flex-wrap gap-3">
        <button
          onclick={handleSearch}
          disabled={isLoading}
          class="flex items-center gap-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white rounded-xl font-medium transition-colors"
        >
          {#if isLoading}
            <Loader2 class="w-5 h-5 animate-spin" />
            Searching...
          {:else}
            <SearchIcon class="w-5 h-5" />
            Search
          {/if}
        </button>
        
        <button
          onclick={clearFilters}
          class="px-6 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-xl font-medium transition-colors"
        >
          Clear Filters
        </button>
      </div>
    </div>

    <!-- Results -->
    {#if isLoading}
      <div class="flex items-center justify-center py-20">
        <Loader2 class="w-12 h-12 text-violet-600 animate-spin" />
      </div>
    {:else if hasSearched && photos.length === 0}
      <div class="flex flex-col items-center justify-center py-20 text-center">
        <div class="w-24 h-24 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mb-4">
          <SearchIcon class="w-12 h-12 text-gray-400" />
        </div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">No photos found</h2>
        <p class="text-gray-600 dark:text-gray-400">Try adjusting your search filters</p>
      </div>
    {:else if photos.length > 0}
      <div class="mb-4">
        <p class="text-gray-600 dark:text-gray-400">
          Found {photos.length} photo{photos.length !== 1 ? 's' : ''}
        </p>
      </div>
      
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
    {:else}
      <div class="flex flex-col items-center justify-center py-20 text-center">
        <div class="w-24 h-24 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center mb-4">
          <SearchIcon class="w-12 h-12 text-gray-400" />
        </div>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">Start Searching</h2>
        <p class="text-gray-600 dark:text-gray-400">Add hashtags or filters to find photos</p>
      </div>
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
