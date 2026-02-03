<script lang="ts">
	import { Camera, Github, Twitter } from 'lucide-svelte';
</script>

<footer class="border-t border-slate-200 bg-slate-50 dark:border-slate-800 dark:bg-slate-950">
	<div class="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
		<div class="grid gap-8 md:grid-cols-4">
			<!-- Brand -->
			<div class="md:col-span-2">
				<a href="/" class="flex items-center gap-2">
					<div class="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-violet-600 to-indigo-600">
						<Camera class="h-5 w-5 text-white" />
					</div>
					<span class="text-xl font-bold text-slate-900 dark:text-white">InstaPound</span>
				</a>
				<p class="mt-4 max-w-md text-sm text-slate-600 dark:text-slate-400">
					Upload, browse, and share your photos with the world. Choose a package that fits your needs and start creating today.
				</p>
			</div>

			<!-- Quick Links -->
			<div>
				<h3 class="text-sm font-semibold text-slate-900 dark:text-white">Quick Links</h3>
				<ul class="mt-4 space-y-3">
					<li>
						<a href="/" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							Browse Photos
						</a>
					</li>
					<li>
						<a href="/search" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							Search
						</a>
					</li>
					<li>
						<a href="/pricing" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							Pricing
						</a>
					</li>
				</ul>
			</div>

			<!-- Account -->
			<div>
				<h3 class="text-sm font-semibold text-slate-900 dark:text-white">Account</h3>
				<ul class="mt-4 space-y-3">
					<li>
						<a href="/login" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							Sign In
						</a>
					</li>
					<li>
						<a href="/register" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							Create Account
						</a>
					</li>
					<li>
						<a href="/profile" class="text-sm text-slate-600 transition-colors hover:text-violet-600 dark:text-slate-400 dark:hover:text-violet-400">
							My Profile
						</a>
					</li>
				</ul>
			</div>
		</div>

		<div class="mt-12 flex flex-col items-center justify-between gap-4 border-t border-slate-200 pt-8 dark:border-slate-800 md:flex-row">
			<p class="text-sm text-slate-500 dark:text-slate-500">
				© {new Date().getFullYear()} InstaPound. All rights reserved.
			</p>
			<div class="flex items-center gap-4">
				<a href="https://github.com" class="text-slate-400 transition-colors hover:text-slate-600 dark:hover:text-slate-300">
					<Github class="h-5 w-5" />
				</a>
				<a href="https://twitter.com" class="text-slate-400 transition-colors hover:text-slate-600 dark:hover:text-slate-300">
					<Twitter class="h-5 w-5" />
				</a>
			</div>
		</div>
	</div>
</footer>
