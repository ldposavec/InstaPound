<script lang="ts">
  import type { Photo, DownloadOptions, ImageFormat } from '../types';
  import { X, Download, Loader2, Settings } from 'lucide-svelte';
  import { photoService } from '../services/api';
  import { toastStore } from '../stores/index.svelte';

  interface Props {
    photo: Photo;
    onClose: () => void;
  }

  let { photo, onClose }: Props = $props();

  let downloading = $state(false);
  let downloadOriginal = $state(true);
  
  let options = $state<DownloadOptions>({
    resize: undefined,
    format: undefined,
    applySepia: false,
    applyBlur: 0,
    applyGrayscale: false
  });

  let customWidth = $state(photo.width);
  let customHeight = $state(photo.height);
  let maintainAspectRatio = $state(true);

  const formats: ImageFormat[] = ['PNG', 'JPG', 'BMP', 'WEBP'];

  function updateWidth(newWidth: number) {
    customWidth = newWidth;
    if (maintainAspectRatio) {
      customHeight = Math.round((newWidth / photo.width) * photo.height);
    }
  }

  function updateHeight(newHeight: number) {
    customHeight = newHeight;
    if (maintainAspectRatio) {
      customWidth = Math.round((newHeight / photo.height) * photo.width);
    }
  }

  async function handleDownload() {
    downloading = true;
    
    try {
      let downloadOptions: DownloadOptions | undefined = undefined;
      
      if (!downloadOriginal) {
        downloadOptions = {
          ...options,
          resize: customWidth !== photo.width || customHeight !== photo.height
            ? { width: customWidth, height: customHeight }
            : undefined
        };
      }
      
      const result = await photoService.downloadPhoto(photo.id, downloadOptions);
      
      if (!result.success) {
        toastStore.error(result.error || 'Download failed');
      }
    } finally {
      downloading = false;
      onClose();
    }
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
  class="fixed inset-0 z-[60] bg-black/60 flex items-center justify-center p-4"
  onclick={onClose}
  role="dialog"
  aria-modal="true"
>
  <!-- Modal content -->
  <div 
    class="bg-white dark:bg-gray-900 rounded-2xl max-w-lg w-full p-6 shadow-xl"
    onclick={(e: MouseEvent) => e.stopPropagation()}
  >
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
        <Download class="w-6 h-6 text-violet-500" />
        Download Options
      </h2>
      <button
        onclick={onClose}
        class="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
        aria-label="Close"
      >
        <X class="w-5 h-5 text-gray-500" />
      </button>
    </div>

    <!-- Download type selection -->
    <div class="space-y-4 mb-6">
      <label class="flex items-center gap-3 p-4 border-2 rounded-xl cursor-pointer transition-colors {downloadOriginal ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20' : 'border-gray-200 dark:border-gray-700 hover:border-gray-300'}">
        <input
          type="radio"
          bind:group={downloadOriginal}
          value={true}
          class="w-5 h-5 text-violet-600"
        />
        <div>
          <p class="font-medium text-gray-900 dark:text-white">Original Photo</p>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {photo.width}×{photo.height} • {photo.format}
          </p>
        </div>
      </label>

      <label class="flex items-center gap-3 p-4 border-2 rounded-xl cursor-pointer transition-colors {!downloadOriginal ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20' : 'border-gray-200 dark:border-gray-700 hover:border-gray-300'}">
        <input
          type="radio"
          bind:group={downloadOriginal}
          value={false}
          class="w-5 h-5 text-violet-600"
        />
        <div class="flex items-center gap-2">
          <Settings class="w-5 h-5 text-gray-400" />
          <div>
            <p class="font-medium text-gray-900 dark:text-white">With Filters</p>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              Apply resize, format change, and effects
            </p>
          </div>
        </div>
      </label>
    </div>

    <!-- Filter options (only shown when not downloading original) -->
    {#if !downloadOriginal}
      <div class="space-y-6 mb-6 p-4 bg-gray-50 dark:bg-gray-800 rounded-xl">
        <!-- Resize -->
        <div>
          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
            Resize
          </h3>
          <div class="flex items-center gap-4">
            <div class="flex-1">
              <label class="block text-xs text-gray-500 mb-1">Width</label>
              <input
                type="number"
                value={customWidth}
                onchange={(e: Event) => updateWidth(parseInt((e.target as HTMLInputElement).value) || photo.width)}
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                min="1"
                max="10000"
              />
            </div>
            <div class="flex-1">
              <label class="block text-xs text-gray-500 mb-1">Height</label>
              <input
                type="number"
                value={customHeight}
                onchange={(e: Event) => updateHeight(parseInt((e.target as HTMLInputElement).value) || photo.height)}
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                min="1"
                max="10000"
              />
            </div>
          </div>
          <label class="flex items-center gap-2 mt-2">
            <input
              type="checkbox"
              bind:checked={maintainAspectRatio}
              class="w-4 h-4 text-violet-600 rounded"
            />
            <span class="text-sm text-gray-600 dark:text-gray-400">Maintain aspect ratio</span>
          </label>
        </div>

        <!-- Format -->
        <div>
          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
            Format
          </h3>
          <div class="flex flex-wrap gap-2">
            {#each formats as format}
              <button
                onclick={() => options.format = options.format === format ? undefined : format}
                class="px-4 py-2 rounded-lg text-sm font-medium transition-colors {options.format === format ? 'bg-violet-600 text-white' : 'bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 border border-gray-300 dark:border-gray-600 hover:border-violet-500'}"
              >
                {format}
              </button>
            {/each}
          </div>
        </div>

        <!-- Effects -->
        <div>
          <h3 class="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
            Effects
          </h3>
          <div class="space-y-3">
            <label class="flex items-center gap-3">
              <input
                type="checkbox"
                bind:checked={options.applySepia}
                class="w-4 h-4 text-violet-600 rounded"
              />
              <span class="text-gray-700 dark:text-gray-300">Sepia</span>
            </label>
            <label class="flex items-center gap-3">
              <input
                type="checkbox"
                bind:checked={options.applyGrayscale}
                class="w-4 h-4 text-violet-600 rounded"
              />
              <span class="text-gray-700 dark:text-gray-300">Grayscale</span>
            </label>
            <div>
              <label class="flex items-center gap-3 mb-2">
                <input
                  type="checkbox"
                  checked={options.applyBlur && options.applyBlur > 0}
                  onchange={(e: Event) => options.applyBlur = (e.target as HTMLInputElement).checked ? 5 : 0}
                  class="w-4 h-4 text-violet-600 rounded"
                />
                <span class="text-gray-700 dark:text-gray-300">Blur</span>
              </label>
              {#if options.applyBlur && options.applyBlur > 0}
                <input
                  type="range"
                  bind:value={options.applyBlur}
                  min="1"
                  max="20"
                  class="w-full"
                />
                <p class="text-xs text-gray-500 mt-1">Blur amount: {options.applyBlur}px</p>
              {/if}
            </div>
          </div>
        </div>
      </div>
    {/if}

    <!-- Download button -->
    <button
      onclick={handleDownload}
      disabled={downloading}
      class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white rounded-lg font-medium transition-colors"
    >
      {#if downloading}
        <Loader2 class="w-5 h-5 animate-spin" />
        Downloading...
      {:else}
        <Download class="w-5 h-5" />
        Download
      {/if}
    </button>
  </div>
</div>
