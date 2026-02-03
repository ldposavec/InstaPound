<script lang="ts">
  import { push } from 'svelte-spa-router';
  import { Mail, Lock, User, Loader2, Github, Chrome, Check } from 'lucide-svelte';
  import { authService } from '../services/api';
  import { toastStore } from '../stores/index.svelte';
  import type { RegisterData, PackageType } from '../types';
  import { PACKAGE_LIMITS } from '../types';

  let formData = $state<RegisterData>({
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    selectedPackage: 'FREE'
  });
  
  let isLoading = $state(false);
  let isGoogleLoading = $state(false);
  let isGithubLoading = $state(false);
  let step = $state(1); // 1 = account details, 2 = package selection

  const packages: { type: PackageType; name: string; color: string; features: string[] }[] = [
    {
      type: 'FREE',
      name: 'Free',
      color: 'gray',
      features: [
        '5MB max upload size',
        '5 uploads per day',
        '50 photos storage'
      ]
    },
    {
      type: 'PRO',
      name: 'Pro',
      color: 'violet',
      features: [
        '25MB max upload size',
        '25 uploads per day',
        '500 photos storage'
      ]
    },
    {
      type: 'GOLD',
      name: 'Gold',
      color: 'amber',
      features: [
        '100MB max upload size',
        '100 uploads per day',
        '5000 photos storage'
      ]
    }
  ];

  function validateStep1(): boolean {
    if (!formData.email || !formData.username || !formData.password || !formData.confirmPassword) {
      toastStore.error('Please fill in all fields');
      return false;
    }
    
    if (formData.password.length < 6) {
      toastStore.error('Password must be at least 6 characters');
      return false;
    }
    
    if (formData.password !== formData.confirmPassword) {
      toastStore.error('Passwords do not match');
      return false;
    }
    
    return true;
  }

  function goToStep2() {
    if (validateStep1()) {
      step = 2;
    }
  }

  async function handleSubmit() {
    isLoading = true;
    
    try {
      const result = await authService.register(formData);
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'Registration failed');
      }
    } finally {
      isLoading = false;
    }
  }

  async function handleGoogleLogin() {
    isGoogleLoading = true;
    
    try {
      const result = await authService.loginWithGoogle();
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'Google login failed');
      }
    } finally {
      isGoogleLoading = false;
    }
  }

  async function handleGithubLogin() {
    isGithubLoading = true;
    
    try {
      const result = await authService.loginWithGithub();
      
      if (result.success) {
        push('/dashboard');
      } else {
        toastStore.error(result.error || 'GitHub login failed');
      }
    } finally {
      isGithubLoading = false;
    }
  }
</script>

<div class="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-gray-50 dark:bg-gray-900">
  <div class="max-w-md w-full">
    <!-- Header -->
    <div class="text-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        {step === 1 ? 'Create Account' : 'Choose Your Plan'}
      </h1>
      <p class="mt-2 text-gray-600 dark:text-gray-400">
        {step === 1 ? 'Join InstaPound and start sharing your photos' : 'Select a package that fits your needs'}
      </p>
    </div>

    <!-- Progress indicator -->
    <div class="flex items-center justify-center gap-2 mb-8">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-full flex items-center justify-center {step >= 1 ? 'bg-violet-600 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-500'}">
          {#if step > 1}
            <Check class="w-5 h-5" />
          {:else}
            1
          {/if}
        </div>
        <span class="text-sm text-gray-600 dark:text-gray-400">Account</span>
      </div>
      <div class="w-12 h-0.5 bg-gray-200 dark:bg-gray-700 {step >= 2 ? 'bg-violet-600' : ''}"></div>
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-full flex items-center justify-center {step >= 2 ? 'bg-violet-600 text-white' : 'bg-gray-200 dark:bg-gray-700 text-gray-500'}">
          2
        </div>
        <span class="text-sm text-gray-600 dark:text-gray-400">Package</span>
      </div>
    </div>

    <div class="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-8">
      {#if step === 1}
        <!-- Step 1: Account Details -->
        
        <!-- Social Login Buttons -->
        <div class="space-y-3 mb-6">
          <button
            onclick={handleGoogleLogin}
            disabled={isGoogleLoading || isLoading || isGithubLoading}
            class="w-full flex items-center justify-center gap-3 px-4 py-3 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-600 rounded-xl font-medium text-gray-700 dark:text-gray-200 transition-colors disabled:opacity-50"
          >
            {#if isGoogleLoading}
              <Loader2 class="w-5 h-5 animate-spin" />
            {:else}
              <Chrome class="w-5 h-5 text-red-500" />
            {/if}
            Continue with Google
          </button>
          
          <button
            onclick={handleGithubLogin}
            disabled={isGithubLoading || isLoading || isGoogleLoading}
            class="w-full flex items-center justify-center gap-3 px-4 py-3 bg-gray-900 dark:bg-gray-600 hover:bg-gray-800 dark:hover:bg-gray-500 text-white rounded-xl font-medium transition-colors disabled:opacity-50"
          >
            {#if isGithubLoading}
              <Loader2 class="w-5 h-5 animate-spin" />
            {:else}
              <Github class="w-5 h-5" />
            {/if}
            Continue with GitHub
          </button>
        </div>

        <!-- Divider -->
        <div class="relative mb-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200 dark:border-gray-700"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-4 bg-white dark:bg-gray-800 text-gray-500">or create with email</span>
          </div>
        </div>

        <!-- Form -->
        <div class="space-y-5">
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Email Address
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Mail class="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="email"
                id="email"
                bind:value={formData.email}
                placeholder="you@example.com"
                class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
                required
              />
            </div>
          </div>

          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Username
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <User class="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="text"
                id="username"
                bind:value={formData.username}
                placeholder="johndoe"
                class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
                required
              />
            </div>
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Password
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Lock class="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="password"
                id="password"
                bind:value={formData.password}
                placeholder="••••••••"
                class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
                required
              />
            </div>
          </div>

          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Confirm Password
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Lock class="h-5 w-5 text-gray-400" />
              </div>
              <input
                type="password"
                id="confirmPassword"
                bind:value={formData.confirmPassword}
                placeholder="••••••••"
                class="w-full pl-11 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-xl bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-violet-500 focus:border-transparent transition-all"
                required
              />
            </div>
          </div>

          <button
            onclick={goToStep2}
            class="w-full flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 text-white rounded-xl font-semibold transition-colors"
          >
            Continue to Package Selection
          </button>
        </div>
      {:else}
        <!-- Step 2: Package Selection -->
        <div class="space-y-4">
          {#each packages as pkg}
            {@const limits = PACKAGE_LIMITS[pkg.type]}
            <button
              onclick={() => formData.selectedPackage = pkg.type}
              class="w-full p-4 border-2 rounded-xl text-left transition-all {formData.selectedPackage === pkg.type 
                ? pkg.type === 'FREE' 
                  ? 'border-gray-500 bg-gray-50 dark:bg-gray-700/50' 
                  : pkg.type === 'PRO'
                    ? 'border-violet-500 bg-violet-50 dark:bg-violet-900/20'
                    : 'border-amber-500 bg-amber-50 dark:bg-amber-900/20'
                : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'}"
            >
              <div class="flex items-center justify-between mb-2">
                <div>
                  <h3 class="font-bold text-gray-900 dark:text-white">{pkg.name}</h3>
                  <p class="text-lg font-semibold {pkg.type === 'FREE' ? 'text-gray-600 dark:text-gray-400' : pkg.type === 'PRO' ? 'text-violet-600 dark:text-violet-400' : 'text-amber-600 dark:text-amber-400'}">
                    ${limits.price}/month
                  </p>
                </div>
                <div class="w-6 h-6 rounded-full border-2 flex items-center justify-center {formData.selectedPackage === pkg.type 
                  ? pkg.type === 'FREE' 
                    ? 'border-gray-500 bg-gray-500' 
                    : pkg.type === 'PRO'
                      ? 'border-violet-500 bg-violet-500'
                      : 'border-amber-500 bg-amber-500'
                  : 'border-gray-300 dark:border-gray-600'}">
                  {#if formData.selectedPackage === pkg.type}
                    <Check class="w-4 h-4 text-white" />
                  {/if}
                </div>
              </div>
              <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                {#each pkg.features as feature}
                  <li class="flex items-center gap-2">
                    <span class="w-1.5 h-1.5 rounded-full {pkg.type === 'FREE' ? 'bg-gray-400' : pkg.type === 'PRO' ? 'bg-violet-400' : 'bg-amber-400'}"></span>
                    {feature}
                  </li>
                {/each}
              </ul>
            </button>
          {/each}

          <div class="flex gap-3 pt-4">
            <button
              onclick={() => step = 1}
              class="flex-1 px-4 py-3 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-xl font-medium transition-colors"
            >
              Back
            </button>
            <button
              onclick={handleSubmit}
              disabled={isLoading}
              class="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-violet-600 hover:bg-violet-700 disabled:bg-violet-400 text-white rounded-xl font-semibold transition-colors"
            >
              {#if isLoading}
                <Loader2 class="w-5 h-5 animate-spin" />
                Creating...
              {:else}
                Create Account
              {/if}
            </button>
          </div>
        </div>
      {/if}
    </div>

    <!-- Sign in link -->
    <p class="mt-6 text-center text-gray-600 dark:text-gray-400">
      Already have an account?
      <button
        onclick={() => push('/login')}
        class="text-violet-600 dark:text-violet-400 hover:text-violet-700 dark:hover:text-violet-300 font-medium ml-1"
      >
        Sign in
      </button>
    </p>
  </div>
</div>
