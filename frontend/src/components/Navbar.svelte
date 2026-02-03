<script lang="ts">
  import { 
    Home, 
    Upload, 
    Image, 
    Search, 
    User, 
    Settings, 
    LogOut, 
    Menu, 
    X,
    Sun,
    Moon,
    Shield,
    Camera
  } from 'lucide-svelte';
  import { push } from 'svelte-spa-router';
  import { authStore, themeStore } from '../stores/index.svelte';
  import { authService } from '../services/api';

  let mobileMenuOpen = $state(false);
  
  const navItems = [
    { path: '/', label: 'Home', icon: Home },
    { path: '/browse', label: 'Browse', icon: Image },
    { path: '/search', label: 'Search', icon: Search },
  ];
  
  const authNavItems = [
    { path: '/upload', label: 'Upload', icon: Upload },
    { path: '/dashboard', label: 'Dashboard', icon: User },
  ];
  
  const adminNavItems = [
    { path: '/admin', label: 'Admin', icon: Shield },
  ];

  function handleLogout() {
    authService.logout();
    push('/');
    mobileMenuOpen = false;
  }
  
  function navigate(path: string) {
    push(path);
    mobileMenuOpen = false;
  }
</script>

<nav class="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-40">
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
    <div class="flex justify-between h-16">
      <!-- Logo and brand -->
      <div class="flex items-center">
        <button 
          onclick={() => navigate('/')}
          class="flex items-center gap-2 text-xl font-bold text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 transition-colors"
        >
          <Camera class="w-8 h-8" />
          <span class="hidden sm:inline">InstaPound</span>
        </button>
      </div>

      <!-- Desktop navigation -->
      <div class="hidden md:flex items-center gap-1">
        {#each navItems as item}
          <button
            onclick={() => navigate(item.path)}
            class="flex items-center gap-2 px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          >
            <svelte:component this={item.icon} class="w-4 h-4" />
            {item.label}
          </button>
        {/each}
        
        {#if authStore.isAuthenticated}
          {#each authNavItems as item}
            <button
              onclick={() => navigate(item.path)}
              class="flex items-center gap-2 px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <svelte:component this={item.icon} class="w-4 h-4" />
              {item.label}
            </button>
          {/each}
          
          {#if authStore.isAdmin}
            {#each adminNavItems as item}
              <button
                onclick={() => navigate(item.path)}
                class="flex items-center gap-2 px-4 py-2 text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors"
              >
                <svelte:component this={item.icon} class="w-4 h-4" />
                {item.label}
              </button>
            {/each}
          {/if}
        {/if}
      </div>

      <!-- Right side actions -->
      <div class="flex items-center gap-2">
        <!-- Theme toggle -->
        <button
          onclick={() => themeStore.toggle()}
          class="p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          aria-label="Toggle theme"
        >
          {#if themeStore.isDark}
            <Sun class="w-5 h-5" />
          {:else}
            <Moon class="w-5 h-5" />
          {/if}
        </button>

        <!-- Auth buttons -->
        {#if authStore.isAuthenticated}
          <div class="hidden md:flex items-center gap-2">
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {authStore.user?.username}
            </span>
            <button
              onclick={handleLogout}
              class="flex items-center gap-2 px-4 py-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
            >
              <LogOut class="w-4 h-4" />
              Logout
            </button>
          </div>
        {:else}
          <div class="hidden md:flex items-center gap-2">
            <button
              onclick={() => navigate('/login')}
              class="px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              Login
            </button>
            <button
              onclick={() => navigate('/register')}
              class="px-4 py-2 bg-violet-600 text-white hover:bg-violet-700 rounded-lg transition-colors"
            >
              Sign Up
            </button>
          </div>
        {/if}

        <!-- Mobile menu button -->
        <button
          onclick={() => mobileMenuOpen = !mobileMenuOpen}
          class="md:hidden p-2 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          aria-label="Toggle menu"
        >
          {#if mobileMenuOpen}
            <X class="w-6 h-6" />
          {:else}
            <Menu class="w-6 h-6" />
          {/if}
        </button>
      </div>
    </div>
  </div>

  <!-- Mobile menu -->
  {#if mobileMenuOpen}
    <div class="md:hidden border-t border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900">
      <div class="px-4 py-3 space-y-1">
        {#each navItems as item}
          <button
            onclick={() => navigate(item.path)}
            class="flex w-full items-center gap-3 px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          >
            <svelte:component this={item.icon} class="w-5 h-5" />
            {item.label}
          </button>
        {/each}
        
        {#if authStore.isAuthenticated}
          {#each authNavItems as item}
            <button
              onclick={() => navigate(item.path)}
              class="flex w-full items-center gap-3 px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
            >
              <svelte:component this={item.icon} class="w-5 h-5" />
              {item.label}
            </button>
          {/each}
          
          {#if authStore.isAdmin}
            {#each adminNavItems as item}
              <button
                onclick={() => navigate(item.path)}
                class="flex w-full items-center gap-3 px-4 py-3 text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors"
              >
                <svelte:component this={item.icon} class="w-5 h-5" />
                {item.label}
              </button>
            {/each}
          {/if}
          
          <hr class="border-gray-200 dark:border-gray-800 my-2" />
          
          <div class="px-4 py-2 text-sm text-gray-600 dark:text-gray-400">
            Signed in as <strong>{authStore.user?.username}</strong>
          </div>
          
          <button
            onclick={handleLogout}
            class="flex w-full items-center gap-3 px-4 py-3 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
          >
            <LogOut class="w-5 h-5" />
            Logout
          </button>
        {:else}
          <hr class="border-gray-200 dark:border-gray-800 my-2" />
          
          <button
            onclick={() => navigate('/login')}
            class="flex w-full items-center gap-3 px-4 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg transition-colors"
          >
            Login
          </button>
          <button
            onclick={() => navigate('/register')}
            class="flex w-full items-center gap-3 px-4 py-3 bg-violet-600 text-white hover:bg-violet-700 rounded-lg transition-colors"
          >
            Sign Up
          </button>
        {/if}
      </div>
    </div>
  {/if}
</nav>
