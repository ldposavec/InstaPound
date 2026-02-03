<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { authStore, usageStore, toastStore } from '../stores/index.svelte';
  import { photoService } from '../services/api';
  import { PACKAGE_LIMITS, type ImageFormat, type ProcessingOptions } from '../types';
  import { onMount } from 'svelte';
  import { 
    Upload,
    X,
    Plus,
    Hash,
    FileImage,
    Maximize,
    AlertCircle,
    Loader2,
    CheckCircle,
    Image
  } from 'lucide-svelte';

  let dragOver = $state(false);
  let selectedFile = $state<File | null>(null);
  let previewUrl = $state<string | null>(null);
  let description = $state('');
  let hashtags = $state<string[]>([]);
  let hashtagInput = $state('');
  let isUploading = $state(false);
  
  let processingOptions = $state<ProcessingOptions>({
    format: undefined,
    resize: undefined
  });
  
  let useResize = $state(false);
  let resizeWidth = $state(1920);
  let resizeHeight = $state(1080);
  let maintainAspectRatio = $state(true);
  let originalAspectRatio = $state(1);

  const formats: ImageFormat[] = ['PNG', 'JPG', 'BMP', 'WEBP'];

  $effect(() => {
    if (!authStore.isAuthenticated) {
      push('/login');
    }
  });

  function handleDrop(e: DragEvent) {
    e.preventDefault();
    dragOver = false;
    
    const file = e.dataTransfer?.files[0];
    if (file && file.type.startsWith('image/')) {
      selectFile(file);
    } else {
      toastStore.error('Please drop an image file');
    }
  }

  function handleFileSelect(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      selectFile(file);
    }
  }

  function selectFile(file: File) {
    if (!authStore.user) return;
    
    const limits = PACKAGE_LIMITS[authStore.user.currentPackage];
    const maxSizeBytes = limits.maxUploadSizeMB * 1024 * 1024;
    
    if (file.size > maxSizeBytes) {
      toastStore.error(`File size exceeds your limit of ${limits.maxUploadSizeMB}MB`);
      return;
    }
    
    selectedFile = file;
    
    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      previewUrl = e.target?.result as string;
      
      // Get original dimensions
      const img = new window.Image();
      img.onload = () => {
        resizeWidth = img.width;
        resizeHeight = img.height;
        originalAspectRatio = img.width / img.height;
      };
      img.src = previewUrl;
    };
    reader.readAsDataURL(file);
  }

  function clearFile() {
    selectedFile = null;
    previewUrl = null;
    description = '';
    hashtags = [];
    hashtagInput = '';
    useResize = false;
    processingOptions = { format: undefined, resize: undefined };
  }

  function addHashtag() {
    const tag = hashtagInput.trim().toLowerCase().replace('#', '');
    if (tag && !hashtags.includes(tag)) {
      hashtags = [...hashtags, tag];
      hashtagInput = '';
    }
  }

  function removeHashtag(tag: string) {
    hashtags = hashtags.filter(t => t !== tag);
  }

  function handleHashtagKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      addHashtag();
    }
  }

  function updateWidth(newWidth: number) {
    resizeWidth = newWidth;
    if (maintainAspectRatio) {
      resizeHeight = Math.round(newWidth / originalAspectRatio);
    }
  }

  function updateHeight(newHeight: number) {
    resizeHeight = newHeight;
    if (maintainAspectRatio) {
      resizeWidth = Math.round(newHeight * originalAspectRatio);
    }
  }

  async function handleUpload() {
    if (!selectedFile) {
      toastStore.error('Please select a file');
      return;
    }
    
    if (!authStore.user) return;
    
    // Check daily limit
    const limits = PACKAGE_LIMITS[authStore.user.currentPackage];
    if (usageStore.usage.uploadedToday >= limits.dailyUploadLimit) {
      toastStore.error('You have reached your daily upload limit');
      return;
    }
    
    isUploading = true;
    
    try {
      const options: ProcessingOptions = {
        ...processingOptions
      };
      
      if (useResize) {
        options.resize = { width: resizeWidth, height: resizeHeight };
      }
      
      const result = await photoService.uploadPhoto({
        file: selectedFile,
        description,
        hashtags,
        processingOptions: options
      });
      
      if (result.success) {
        clearFile();
        push('/browse');
      } else {
        toastStore.error(result.error || 'Upload failed');
      }
    } finally {
      isUploading = false;
    }
  }
</script>

{#if authStore.user}
  {@const limits = PACKAGE_LIMITS[authStore.user.currentPackage]}
  {@const usage = usageStore.usage}
  {@const canUpload = usage.uploadedToday < limits.dailyUploadLimit}

  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Upload Photo
        </h1>
        <p class="text-gray-600 dark:text-gray-400">
          Share your photos with the world
        </p>
      </div>

      <!-- Usage Warning -->
      {#if !canUpload}
        <div class="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl flex items-start gap-3">
          <AlertCircle class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
          <div>
            <p class="font-medium text-red-800 dark:text-red-200">Daily Limit Reached</p>
            <p class="text-sm text-red-600 dark:text-red-300 mt-1">
              You've reached your daily upload limit of {limits.dailyUploadLimit} photos. 
              Upgrade your package for more uploads.
            </p>
          </div>
        </div>
      {:else}
        <div class="mb-6 p-4 bg-violet-50 dark:bg-violet-900/20 border border-violet-200 dark:border-violet-800 rounded-xl">
          <p class="text-sm text-violet-700 dark:text-violet-300">
            <strong>{usage.uploadedToday}/{limits.dailyUploadLimit}</strong> uploads used today • 
            Max file size: <strong>{limits.maxUploadSizeMB}MB</strong>
          </p>
        </div>
      {/if}

      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm overflow-hidden">
        {#if !selectedFile}
          <!-- Drop Zone -->
          <div
            class="p-12 border-2 border-dashed transition-colors {dragOver 
              ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20' 
              : 'border-gray-300 dark:border-gray-600 hover:border-gray-400 dark:hover:border-gray-500'}"
            ondragover={(e) => { e.preventDefault(); dragOver = true; }}
            ondragleave={() => dragOver = false}
            ondrop={handleDrop}
            role="button"
            tabindex="0"
          >
            <div class="text-center">
              <div class="w-16 h-16 mx-auto mb-4 bg-violet-100 dark:bg-violet-900/30 rounded-full flex items-center justify-center">
                <Upload class="w-8 h-8 text-violet-600 dark:text-violet-400" />
              </div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                Drop your image here
              </h3>
              <p class="text-gray-500 dark:text-gray-400 mb-4">
                or click to browse
              </p>
              <label class="inline-flex items-center gap-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-medium cursor-pointer transition-colors">
                <Plus class="w-5 h-5" />
                Choose File
                <input
                  type="file"
                  accept="image/*"
                  onchange={handleFileSelect}
                  class="hidden"
                  disabled={!canUpload}
                />
              </label>
              <p class="text-xs text-gray-400 dark:text-gray-500 mt-4">
                Supports: PNG, JPG, JPEG, GIF, BMP, WEBP
              </p>
            </div>
          </div>
        {:else}
          <!-- Upload Form -->
          <div class="p-6">
            <div class="flex items-start gap-6">
              <!-- Preview -->
              <div class="relative flex-shrink-0">
                <img
                  src={previewUrl}
                  alt="Preview"
                  class="w-48 h-48 object-cover rounded-xl"
                />
                <button
                  onclick={clearFile}
                  class="absolute -top-2 -right-2 p-1 bg-red-500 hover:bg-red-600 text-white rounded-full shadow-lg transition-colors"
                >
                  <X class="w-4 h-4" />
                </button>
              </div>

              <!-- Info -->
              <div class="flex-1">
                <h3 class="font-medium text-gray-900 dark:text-white mb-1">
                  {selectedFile.name}
                </h3>
                <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
                  {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB
                </p>

                <!-- Description -->
                <div class="mb-4">
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Description
                  </label>
                  <textarea
                    bind:value={description}
                    placeholder="Add a description for your photo..."
                    rows="3"
                    class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 resize-none"
                  ></textarea>
                </div>

                <!-- Hashtags -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    <Hash class="w-4 h-4 inline mr-1" />
                    Hashtags
                  </label>
                  <div class="flex gap-2">
                    <input
                      type="text"
                      bind:value={hashtagInput}
                      onkeydown={handleHashtagKeydown}
                      placeholder="Add hashtags..."
                      class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500"
                    />
                    <button
                      onclick={addHashtag}
                      class="px-4 py-2 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-xl hover:bg-violet-200 dark:hover:bg-violet-900/50 transition-colors"
                    >
                      Add
                    </button>
                  </div>
                  {#if hashtags.length > 0}
                    <div class="flex flex-wrap gap-2 mt-2">
                      {#each hashtags as tag}
                        <span class="inline-flex items-center gap-1 px-3 py-1 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 rounded-full text-sm">
                          #{tag}
                          <button onclick={() => removeHashtag(tag)} class="hover:text-violet-900 dark:hover:text-violet-100">
                            <X class="w-3 h-3" />
                          </button>
                        </span>
                      {/each}
                    </div>
                  {/if}
                </div>
              </div>
            </div>

            <!-- Processing Options -->
            <div class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
                <FileImage class="w-5 h-5" />
                Processing Options
              </h3>

              <div class="grid md:grid-cols-2 gap-6">
                <!-- Format -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Output Format
                  </label>
                  <div class="flex flex-wrap gap-2">
                    <button
                      onclick={() => processingOptions.format = undefined}
                      class="px-4 py-2 rounded-lg text-sm font-medium transition-colors {processingOptions.format === undefined 
                        ? 'bg-violet-600 text-white' 
                        : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'}"
                    >
                      Original
                    </button>
                    {#each formats as format}
                      <button
                        onclick={() => processingOptions.format = format}
                        class="px-4 py-2 rounded-lg text-sm font-medium transition-colors {processingOptions.format === format 
                          ? 'bg-violet-600 text-white' 
                          : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'}"
                      >
                        {format}
                      </button>
                    {/each}
                  </div>
                </div>

                <!-- Resize -->
                <div>
                  <label class="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    <input
                      type="checkbox"
                      bind:checked={useResize}
                      class="w-4 h-4 text-violet-600 rounded"
                    />
                    <Maximize class="w-4 h-4" />
                    Resize Image
                  </label>
                  
                  {#if useResize}
                    <div class="flex items-center gap-4 mt-2">
                      <div class="flex-1">
                        <label class="block text-xs text-gray-500 mb-1">Width</label>
                        <input
                          type="number"
                          value={resizeWidth}
                          onchange={(e) => updateWidth(parseInt((e.target as HTMLInputElement).value) || 1920)}
                          class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                          min="1"
                          max="10000"
                        />
                      </div>
                      <div class="flex-1">
                        <label class="block text-xs text-gray-500 mb-1">Height</label>
                        <input
                          type="number"
                          value={resizeHeight}
                          onchange={(e) => updateHeight(parseInt((e.target as HTMLInputElement).value) || 1080)}
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
                  {/if}
                </div>
              </div>
            </div>

            <!-- Upload Button -->
            <div class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
              <button
                onclick={handleUpload}
                disabled={isUploading || !canUpload}
                class="w-full flex items-center justify-center gap-2 px-6 py-4 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white rounded-xl font-semibold text-lg transition-colors"
              >
                {#if isUploading}
                  <Loader2 class="w-6 h-6 animate-spin" />
                  Uploading...
                {:else}
                  <CheckCircle class="w-6 h-6" />
                  Upload Photo
                {/if}
              </button>
            </div>
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}
