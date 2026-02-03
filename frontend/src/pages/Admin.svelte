<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { authStore, toastStore } from '../stores/index.svelte';
  import { adminService, photoService } from '../services/api';
  import { PACKAGE_LIMITS, type User, type UserStats, type ActionLog, type Photo, type PackageType } from '../types';
  import { onMount } from 'svelte';
  import PhotoCard from '../components/PhotoCard.svelte';
  import { 
    Users, 
    Image, 
    Activity, 
    Settings,
    Search,
    ChevronDown,
    Edit,
    Trash2,
    Shield,
    Crown,
    Sparkles,
    Package,
    Loader2,
    RefreshCw,
    Eye,
    Clock,
    HardDrive,
    X
  } from 'lucide-svelte';

  let activeTab = $state<'users' | 'photos' | 'logs'>('users');
  
  // Users
  let users = $state<User[]>([]);
  let userStats = $state<UserStats[]>([]);
  let isLoadingUsers = $state(true);
  let selectedUser = $state<User | null>(null);
  let showUserModal = $state(false);
  
  // Photos
  let photos = $state<Photo[]>([]);
  let isLoadingPhotos = $state(true);
  
  // Logs
  let logs = $state<ActionLog[]>([]);
  let isLoadingLogs = $state(true);
  let logsPage = $state(1);
  let totalLogs = $state(0);

  $effect(() => {
    if (!authStore.isAuthenticated || !authStore.isAdmin) {
      push('/');
    }
  });

  onMount(async () => {
    await loadData();
  });

  async function loadData() {
    await Promise.all([
      loadUsers(),
      loadPhotos(),
      loadLogs()
    ]);
  }

  async function loadUsers() {
    isLoadingUsers = true;
    try {
      const [usersResult, statsResult] = await Promise.all([
        adminService.getUsers(),
        adminService.getUserStats()
      ]);
      
      if (usersResult.success && usersResult.data) {
        users = usersResult.data;
      }
      if (statsResult.success && statsResult.data) {
        userStats = statsResult.data;
      }
    } finally {
      isLoadingUsers = false;
    }
  }

  async function loadPhotos() {
    isLoadingPhotos = true;
    try {
      const result = await photoService.getPhotos(1, 50);
      if (result.success && result.data) {
        photos = result.data.items;
      }
    } finally {
      isLoadingPhotos = false;
    }
  }

  async function loadLogs(page: number = 1) {
    isLoadingLogs = true;
    try {
      const result = await adminService.getActionLogs(page, 20);
      if (result.success && result.data) {
        logs = result.data.items;
        totalLogs = result.data.total;
        logsPage = result.data.page;
      }
    } finally {
      isLoadingLogs = false;
    }
  }

  function getStatForUser(userId: string): UserStats | undefined {
    return userStats.find(s => s.userId === userId);
  }

  function openUserModal(user: User) {
    selectedUser = { ...user };
    showUserModal = true;
  }

  async function updateUser() {
    if (!selectedUser) return;
    
    const result = await adminService.updateUser(selectedUser.id, {
      role: selectedUser.role,
      currentPackage: selectedUser.currentPackage
    });
    
    if (result.success) {
      await loadUsers();
      showUserModal = false;
      selectedUser = null;
    } else {
      toastStore.error(result.error || 'Failed to update user');
    }
  }

  async function deletePhoto(photo: Photo) {
    if (!confirm('Are you sure you want to delete this photo?')) return;
    
    const result = await adminService.deleteUserPhoto(photo.id);
    if (result.success) {
      await loadPhotos();
    } else {
      toastStore.error(result.error || 'Failed to delete photo');
    }
  }

  function formatDate(date: string): string {
    return new Date(date).toLocaleString();
  }

  function getPackageIcon(pkg: PackageType) {
    switch (pkg) {
      case 'GOLD': return Crown;
      case 'PRO': return Sparkles;
      default: return Package;
    }
  }

  const tabs = [
    { id: 'users' as const, label: 'Users', icon: Users },
    { id: 'photos' as const, label: 'Photos', icon: Image },
    { id: 'logs' as const, label: 'Activity Logs', icon: Activity }
  ];
</script>

{#if authStore.isAdmin}
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-10 h-10 bg-violet-100 dark:bg-violet-900/30 rounded-lg flex items-center justify-center">
            <Shield class="w-5 h-5 text-violet-600 dark:text-violet-400" />
          </div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
            Admin Panel
          </h1>
        </div>
        <p class="text-gray-600 dark:text-gray-400">
          Manage users, photos, and view activity logs
        </p>
      </div>

      <!-- Stats Cards -->
      <div class="grid md:grid-cols-3 gap-6 mb-8">
        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Total Users</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{users.length}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-xl flex items-center justify-center">
              <Users class="w-6 h-6 text-blue-500" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Total Photos</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{photos.length}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-xl flex items-center justify-center">
              <Image class="w-6 h-6 text-green-500" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Total Actions</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-white mt-1">{totalLogs}</p>
            </div>
            <div class="w-12 h-12 bg-violet-100 dark:bg-violet-900/30 rounded-xl flex items-center justify-center">
              <Activity class="w-6 h-6 text-violet-500" />
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-sm">
        <div class="border-b border-gray-200 dark:border-gray-700">
          <nav class="flex">
            {#each tabs as tab}
              <button
                onclick={() => activeTab = tab.id}
                class="flex items-center gap-2 px-6 py-4 text-sm font-medium border-b-2 transition-colors {activeTab === tab.id 
                  ? 'border-violet-500 text-violet-600 dark:text-violet-400' 
                  : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'}"
              >
                <svelte:component this={tab.icon} class="w-5 h-5" />
                {tab.label}
              </button>
            {/each}
          </nav>
        </div>

        <div class="p-6">
          <!-- Users Tab -->
          {#if activeTab === 'users'}
            {#if isLoadingUsers}
              <div class="flex items-center justify-center py-12">
                <Loader2 class="w-8 h-8 text-violet-600 animate-spin" />
              </div>
            {:else}
              <div class="overflow-x-auto">
                <table class="w-full">
                  <thead>
                    <tr class="text-left text-sm text-gray-500 dark:text-gray-400">
                      <th class="pb-4 font-medium">User</th>
                      <th class="pb-4 font-medium">Package</th>
                      <th class="pb-4 font-medium">Role</th>
                      <th class="pb-4 font-medium">Photos</th>
                      <th class="pb-4 font-medium">Storage</th>
                      <th class="pb-4 font-medium">Last Active</th>
                      <th class="pb-4 font-medium">Actions</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-100 dark:divide-gray-700">
                    {#each users as user}
                      {@const stats = getStatForUser(user.id)}
                      <tr>
                        <td class="py-4">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 bg-violet-100 dark:bg-violet-900/30 rounded-full flex items-center justify-center">
                              {#if user.avatarUrl}
                                <img src={user.avatarUrl} alt="" class="w-full h-full rounded-full object-cover" />
                              {:else}
                                <span class="text-violet-600 dark:text-violet-400 font-medium">
                                  {user.username.charAt(0).toUpperCase()}
                                </span>
                              {/if}
                            </div>
                            <div>
                              <p class="font-medium text-gray-900 dark:text-white">{user.username}</p>
                              <p class="text-sm text-gray-500 dark:text-gray-400">{user.email}</p>
                            </div>
                          </div>
                        </td>
                        <td class="py-4">
                          <span class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium {user.currentPackage === 'GOLD' ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300' : user.currentPackage === 'PRO' ? 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-300' : 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300'}">
                            <svelte:component this={getPackageIcon(user.currentPackage)} class="w-3 h-3" />
                            {user.currentPackage}
                          </span>
                        </td>
                        <td class="py-4">
                          <span class="px-2 py-1 rounded-full text-xs font-medium {user.role === 'ADMIN' ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300' : 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'}">
                            {user.role}
                          </span>
                        </td>
                        <td class="py-4 text-gray-900 dark:text-white">
                          {stats?.totalPhotos || 0}
                        </td>
                        <td class="py-4 text-gray-900 dark:text-white">
                          {stats?.storageUsedMB?.toFixed(1) || 0} MB
                        </td>
                        <td class="py-4 text-sm text-gray-500 dark:text-gray-400">
                          {stats?.lastActive ? formatDate(stats.lastActive) : 'Never'}
                        </td>
                        <td class="py-4">
                          <button
                            onclick={() => openUserModal(user)}
                            class="p-2 text-gray-500 hover:text-violet-600 dark:hover:text-violet-400 transition-colors"
                            title="Edit user"
                          >
                            <Edit class="w-5 h-5" />
                          </button>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          {/if}

          <!-- Photos Tab -->
          {#if activeTab === 'photos'}
            {#if isLoadingPhotos}
              <div class="flex items-center justify-center py-12">
                <Loader2 class="w-8 h-8 text-violet-600 animate-spin" />
              </div>
            {:else if photos.length === 0}
              <div class="text-center py-12">
                <Image class="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-4" />
                <p class="text-gray-500 dark:text-gray-400">No photos found</p>
              </div>
            {:else}
              <div class="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {#each photos as photo (photo.id)}
                  <PhotoCard
                    {photo}
                    onDelete={() => deletePhoto(photo)}
                    showActions={true}
                  />
                {/each}
              </div>
            {/if}
          {/if}

          <!-- Logs Tab -->
          {#if activeTab === 'logs'}
            {#if isLoadingLogs}
              <div class="flex items-center justify-center py-12">
                <Loader2 class="w-8 h-8 text-violet-600 animate-spin" />
              </div>
            {:else}
              <div class="space-y-3">
                {#each logs as log}
                  <div class="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                    <div class="w-10 h-10 bg-violet-100 dark:bg-violet-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
                      <Activity class="w-5 h-5 text-violet-500" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <span class="font-medium text-gray-900 dark:text-white">{log.username}</span>
                        <span class="px-2 py-0.5 bg-violet-100 dark:bg-violet-900/30 text-violet-700 dark:text-violet-300 text-xs font-medium rounded">
                          {log.action}
                        </span>
                      </div>
                      <p class="text-sm text-gray-500 dark:text-gray-400 truncate">{log.details}</p>
                    </div>
                    <div class="text-right flex-shrink-0">
                      <p class="text-sm text-gray-500 dark:text-gray-400">
                        {formatDate(log.timestamp)}
                      </p>
                    </div>
                  </div>
                {/each}
              </div>

              <!-- Pagination -->
              <div class="flex justify-center gap-2 mt-6">
                <button
                  onclick={() => loadLogs(logsPage - 1)}
                  disabled={logsPage <= 1}
                  class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  Previous
                </button>
                <span class="px-4 py-2 text-gray-600 dark:text-gray-400">
                  Page {logsPage}
                </span>
                <button
                  onclick={() => loadLogs(logsPage + 1)}
                  disabled={logs.length < 20}
                  class="px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  Next
                </button>
              </div>
            {/if}
          {/if}
        </div>
      </div>
    </div>
  </div>

  <!-- User Edit Modal -->
  {#if showUserModal && selectedUser}
    <div 
      class="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4"
      onclick={() => showUserModal = false}
      role="dialog"
      aria-modal="true"
    >
      <div 
        class="bg-white dark:bg-gray-800 rounded-2xl max-w-md w-full p-6 shadow-xl"
        onclick={(e: MouseEvent) => e.stopPropagation()}
      >
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold text-gray-900 dark:text-white">Edit User</h2>
          <button
            onclick={() => showUserModal = false}
            class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
          >
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Username
            </label>
            <input
              type="text"
              value={selectedUser.username}
              disabled
              class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-gray-50 dark:bg-gray-700 text-gray-500 dark:text-gray-400 cursor-not-allowed"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Role
            </label>
            <select
              bind:value={selectedUser.role}
              class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-violet-500"
            >
              <option value="REGISTERED">Registered</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Package
            </label>
            <select
              bind:value={selectedUser.currentPackage}
              class="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-violet-500"
            >
              <option value="FREE">Free</option>
              <option value="PRO">Pro</option>
              <option value="GOLD">Gold</option>
            </select>
          </div>

          <div class="flex gap-3 pt-4">
            <button
              onclick={() => showUserModal = false}
              class="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-xl font-medium transition-colors"
            >
              Cancel
            </button>
            <button
              onclick={updateUser}
              class="flex-1 px-4 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-medium transition-colors"
            >
              Save Changes
            </button>
          </div>
        </div>
      </div>
    </div>
  {/if}
{/if}
