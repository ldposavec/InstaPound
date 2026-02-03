<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { authStore, usageStore, toastStore } from '../stores/index.svelte';
  import { authService, photoService } from '../services/api';
  import { PACKAGE_LIMITS, type PackageType, type Photo } from '../types';
  import { onMount } from 'svelte';
  import { 
    User, 
    Package, 
    BarChart3, 
    Image, 
    Upload, 
    Calendar, 
    ArrowRight,
    Crown,
    Sparkles,
    RefreshCw,
    Settings,
    Clock,
    HardDrive,
    Zap
  } from 'lucide-svelte';
  import PhotoCard from '../components/PhotoCard.svelte';

  let recentPhotos = $state<Photo[]>([]);
  let isLoadingPhotos = $state(true);
  let isChangingPackage = $state(false);
  let selectedPackage = $state<PackageType | null>(null);

  $effect(() => {
    if (!authStore.isAuthenticated) {
      push('/login');
    }
  });

  onMount(async () => {
    await Promise.all([
      authService.getUsage(),
      loadRecentPhotos()
    ]);
  });

  async function loadRecentPhotos() {
    isLoadingPhotos = true;
    try {
      // In a real app, this would filter by current user
      const result = await photoService.getPhotos(1, 6);
      if (result.success && result.data) {
        recentPhotos = result.data.items.slice(0, 6);
      }
    } finally {
      isLoadingPhotos = false;
    }
  }

  async function handlePackageChange() {
    if (!selectedPackage || selectedPackage === authStore.user?.currentPackage) {
      toastStore.error('Please select a different package');
      return;
    }
    
    isChangingPackage = true;
    
    try {
      const result = await authService.changePackage(selectedPackage);
      if (!result.success) {
        toastStore.error(result.error || 'Failed to change package');
      }
      selectedPackage = null;
    } finally {
      isChangingPackage = false;
    }
  }

  function getUsagePercentage(current: number, max: number): number {
    return Math.min((current / max) * 100, 100);
  }

  const packages: { type: PackageType; name: string; icon: typeof Package }[] = [
    { type: 'FREE', name: 'Free', icon: Package },
    { type: 'PRO', name: 'Pro', icon: Sparkles },
    { type: 'GOLD', name: 'Gold', icon: Crown }
  ];
</script>

{#if authStore.user}
  {@const limits = PACKAGE_LIMITS[authStore.user.currentPackage]}
  {@const usage = usageStore.usage}

  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Welcome Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          Welcome back, {authStore.user.username}!
        </h1>
        <p class="text-gray-600 dark:text-gray-400">
          Manage your photos and track your usage
        </p>
      </div>

      <!-- Stats Grid -->
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <!-- Current Package -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 bg-violet-100 dark:bg-violet-900/30 rounded-xl flex items-center justify-center">
              {#if authStore.user.currentPackage === 'GOLD'}
                <Crown class="w-6 h-6 text-amber-500" />
              {:else if authStore.user.currentPackage === 'PRO'}
                <Sparkles class="w-6 h-6 text-violet-500" />
              {:else}
                <Package class="w-6 h-6 text-gray-500" />
              {/if}
            </div>
            <span class="px-3 py-1 text-xs font-semibold rounded-full {authStore.user.currentPackage === 'GOLD' ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300' : authStore.user.currentPackage === 'PRO' ? 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300' : 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300'}">
              {authStore.user.currentPackage}
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">Current Package</p>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">${limits.price}/mo</p>
          {#if authStore.user.pendingPackage}
            <p class="text-xs text-violet-600 dark:text-violet-400 mt-2 flex items-center gap-1">
              <Clock class="w-3 h-3" />
              Changing to {authStore.user.pendingPackage} tomorrow
            </p>
          {/if}
        </div>

        <!-- Uploads Today -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center">
              <Upload class="w-6 h-6 text-blue-500" />
            </div>
            <span class="text-sm text-gray-500 dark:text-gray-400">
              {usage.uploadedToday}/{limits.dailyUploadLimit}
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">Uploads Today</p>
          <div class="mt-2">
            <div class="w-full h-2 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
              <div 
                class="h-full bg-blue-500 rounded-full transition-all"
                style="width: {getUsagePercentage(usage.uploadedToday, limits.dailyUploadLimit)}%"
              ></div>
            </div>
          </div>
        </div>

        <!-- Total Photos -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-xl flex items-center justify-center">
              <Image class="w-6 h-6 text-green-500" />
            </div>
            <span class="text-sm text-gray-500 dark:text-gray-400">
              {usage.totalStoredPhotos}/{limits.maxStoredPhotos}
            </span>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">Stored Photos</p>
          <div class="mt-2">
            <div class="w-full h-2 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
              <div 
                class="h-full bg-green-500 rounded-full transition-all"
                style="width: {getUsagePercentage(usage.totalStoredPhotos, limits.maxStoredPhotos)}%"
              ></div>
            </div>
          </div>
        </div>

        <!-- Storage Used -->
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 bg-amber-100 dark:bg-amber-900/30 rounded-xl flex items-center justify-center">
              <HardDrive class="w-6 h-6 text-amber-500" />
            </div>
          </div>
          <p class="text-sm text-gray-500 dark:text-gray-400">Storage Used</p>
          <p class="text-2xl font-bold text-gray-900 dark:text-white">{usage.totalStorageMB.toFixed(1)} MB</p>
          <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">Max upload: {limits.maxUploadSizeMB}MB</p>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Left Column: Quick Actions & Recent Photos -->
        <div class="lg:col-span-2 space-y-8">
          <!-- Quick Actions -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Quick Actions</h2>
            <div class="grid sm:grid-cols-3 gap-4">
              <button
                onclick={() => push('/upload')}
                class="flex items-center gap-3 p-4 bg-violet-50 dark:bg-violet-900/20 hover:bg-violet-100 dark:hover:bg-violet-900/30 rounded-xl transition-colors"
              >
                <div class="w-10 h-10 bg-violet-500 rounded-lg flex items-center justify-center">
                  <Upload class="w-5 h-5 text-white" />
                </div>
                <span class="font-medium text-gray-900 dark:text-white">Upload Photo</span>
              </button>
              
              <button
                onclick={() => push('/browse')}
                class="flex items-center gap-3 p-4 bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 dark:hover:bg-blue-900/30 rounded-xl transition-colors"
              >
                <div class="w-10 h-10 bg-blue-500 rounded-lg flex items-center justify-center">
                  <Image class="w-5 h-5 text-white" />
                </div>
                <span class="font-medium text-gray-900 dark:text-white">Browse Gallery</span>
              </button>
              
              <button
                onclick={() => push('/search')}
                class="flex items-center gap-3 p-4 bg-green-50 dark:bg-green-900/20 hover:bg-green-100 dark:hover:bg-green-900/30 rounded-xl transition-colors"
              >
                <div class="w-10 h-10 bg-green-500 rounded-lg flex items-center justify-center">
                  <Zap class="w-5 h-5 text-white" />
                </div>
                <span class="font-medium text-gray-900 dark:text-white">Search Photos</span>
              </button>
            </div>
          </div>

          <!-- Recent Photos -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-lg font-bold text-gray-900 dark:text-white">Recent Photos</h2>
              <button
                onclick={() => push('/browse')}
                class="text-sm text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 flex items-center gap-1"
              >
                View all
                <ArrowRight class="w-4 h-4" />
              </button>
            </div>
            
            {#if isLoadingPhotos}
              <div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4">
                {#each Array(6) as _}
                  <div class="aspect-[3/2] bg-gray-100 dark:bg-gray-700 rounded-xl animate-pulse"></div>
                {/each}
              </div>
            {:else if recentPhotos.length === 0}
              <div class="text-center py-12">
                <Image class="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-4" />
                <p class="text-gray-500 dark:text-gray-400">No photos yet</p>
                <button
                  onclick={() => push('/upload')}
                  class="mt-4 text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 font-medium"
                >
                  Upload your first photo
                </button>
              </div>
            {:else}
              <div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4">
                {#each recentPhotos as photo}
                  <div class="aspect-[3/2] rounded-xl overflow-hidden group relative">
                    <img
                      src={photo.thumbnailUrl}
                      alt={photo.description}
                      class="w-full h-full object-cover"
                    />
                    <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                      <button
                        onclick={() => push(`/photo/${photo.id}`)}
                        class="px-4 py-2 bg-white text-gray-900 rounded-lg text-sm font-medium"
                      >
                        View
                      </button>
                    </div>
                  </div>
                {/each}
              </div>
            {/if}
          </div>
        </div>

        <!-- Right Column: Package Management -->
        <div class="space-y-8">
          <!-- Change Package -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Change Package</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
              You can change your package once per day. Changes take effect the next day.
            </p>
            
            <div class="space-y-3">
              {#each packages as pkg}
                {@const pkgLimits = PACKAGE_LIMITS[pkg.type]}
                <button
                  onclick={() => selectedPackage = pkg.type}
                  disabled={pkg.type === authStore.user.currentPackage}
                  class="w-full p-4 border-2 rounded-xl text-left transition-all disabled:opacity-50 disabled:cursor-not-allowed {selectedPackage === pkg.type 
                    ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20' 
                    : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'}"
                >
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <div class="w-10 h-10 rounded-lg flex items-center justify-center {pkg.type === 'GOLD' ? 'bg-amber-100 dark:bg-amber-900/30' : pkg.type === 'PRO' ? 'bg-violet-100 dark:bg-violet-900/30' : 'bg-gray-100 dark:bg-gray-700'}">
                        <svelte:component 
                          this={pkg.icon} 
                          class="w-5 h-5 {pkg.type === 'GOLD' ? 'text-amber-500' : pkg.type === 'PRO' ? 'text-violet-500' : 'text-gray-500'}" 
                        />
                      </div>
                      <div>
                        <p class="font-medium text-gray-900 dark:text-white">{pkg.name}</p>
                        <p class="text-sm text-gray-500 dark:text-gray-400">${pkgLimits.price}/mo</p>
                      </div>
                    </div>
                    {#if pkg.type === authStore.user.currentPackage}
                      <span class="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-300 text-xs font-medium rounded-full">
                        Current
                      </span>
                    {/if}
                  </div>
                </button>
              {/each}
            </div>
            
            <button
              onclick={handlePackageChange}
              disabled={!selectedPackage || selectedPackage === authStore.user.currentPackage || isChangingPackage}
              class="w-full mt-4 flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 disabled:cursor-not-allowed text-white rounded-xl font-medium transition-colors"
            >
              {#if isChangingPackage}
                <RefreshCw class="w-5 h-5 animate-spin" />
                Processing...
              {:else}
                Change Package
              {/if}
            </button>
          </div>

          <!-- Account Info -->
          <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Account Info</h2>
            <div class="space-y-4">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-violet-100 dark:bg-violet-900/30 rounded-full flex items-center justify-center">
                  {#if authStore.user.avatarUrl}
                    <img src={authStore.user.avatarUrl} alt="" class="w-full h-full rounded-full object-cover" />
                  {:else}
                    <User class="w-6 h-6 text-violet-500" />
                  {/if}
                </div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-white">{authStore.user.username}</p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{authStore.user.email}</p>
                </div>
              </div>
              
              <div class="pt-4 border-t border-gray-100 dark:border-gray-700">
                <div class="flex items-center justify-between text-sm mb-2">
                  <span class="text-gray-500 dark:text-gray-400">Account Type</span>
                  <span class="text-gray-900 dark:text-white font-medium">{authStore.user.role}</span>
                </div>
                <div class="flex items-center justify-between text-sm mb-2">
                  <span class="text-gray-500 dark:text-gray-400">Auth Provider</span>
                  <span class="text-gray-900 dark:text-white font-medium">{authStore.user.authProvider}</span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-500 dark:text-gray-400">Member Since</span>
                  <span class="text-gray-900 dark:text-white font-medium">
                    {new Date(authStore.user.createdAt).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}
