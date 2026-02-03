<script lang="ts">
	import { onMount } from 'svelte';
	import { Users, Camera, Activity, Settings, Shield, Eye, Trash2, Edit, ChevronRight } from 'lucide-svelte';
	import Button from '$lib/components/ui/Button.svelte';
	import Card from '$lib/components/ui/Card.svelte';
	import Alert from '$lib/components/ui/Alert.svelte';
	import Spinner from '$lib/components/ui/Spinner.svelte';
	import Pagination from '$lib/components/ui/Pagination.svelte';
	import Modal from '$lib/components/ui/Modal.svelte';
	import Input from '$lib/components/ui/Input.svelte';
	import Select from '$lib/components/ui/Select.svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import { adminApi } from '$lib/api';
	import { goto } from '$app/navigation';
	import type { User, Photo, ActionLog, Page, UserRole, PackageType } from '$lib/types';

	let activeTab = $state<'users' | 'photos' | 'logs'>('users');
	let loading = $state(true);
	
	// Users
	let usersData = $state<Page<User> | null>(null);
	let selectedUser = $state<User | null>(null);
	let showEditUserModal = $state(false);
	let editUsername = $state('');
	let editEmail = $state('');
	let editRole = $state<UserRole>('REGISTERED');
	let editPackage = $state<PackageType>('FREE');

	// Photos
	let photosData = $state<Page<Photo> | null>(null);

	// Logs
	let logsData = $state<Page<ActionLog> | null>(null);

	let error = $state('');
	let success = $state('');
	let actionLoading = $state(false);

	const roleOptions = [
		{ value: 'REGISTERED', label: 'Registered' },
		{ value: 'ADMIN', label: 'Admin' }
	];

	const packageOptions = [
		{ value: 'FREE', label: 'Free' },
		{ value: 'PRO', label: 'Pro' },
		{ value: 'GOLD', label: 'Gold' }
	];

	onMount(async () => {
		if (!authStore.isAdmin) {
			goto('/');
			return;
		}
		await loadUsers();
		loading = false;
	});

	async function loadUsers(page = 0) {
		try {
			usersData = await adminApi.getAllUsers(page, 10);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load users';
		}
	}

	async function loadPhotos(page = 0) {
		try {
			photosData = await adminApi.getAllPhotos(page, 12);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load photos';
		}
	}

	async function loadLogs(page = 0) {
		try {
			logsData = await adminApi.getAllLogs(page, 20);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load logs';
		}
	}

	function handleTabChange(tab: 'users' | 'photos' | 'logs') {
		activeTab = tab;
		if (tab === 'users' && !usersData) loadUsers();
		if (tab === 'photos' && !photosData) loadPhotos();
		if (tab === 'logs' && !logsData) loadLogs();
	}

	function openEditUser(user: User) {
		selectedUser = user;
		editUsername = user.username;
		editEmail = user.email;
		editRole = user.role;
		editPackage = user.packageType;
		showEditUserModal = true;
	}

	async function handleUpdateUser() {
		if (!selectedUser) return;
		actionLoading = true;
		error = '';
		try {
			await adminApi.updateUser(selectedUser.id, {
				username: editUsername,
				email: editEmail,
				role: editRole,
				packageType: editPackage
			});
			success = 'User updated successfully';
			showEditUserModal = false;
			await loadUsers(usersData?.number || 0);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to update user';
		} finally {
			actionLoading = false;
		}
	}

	async function handleDeleteUser(user: User) {
		if (!confirm(`Are you sure you want to delete user "${user.username}"?`)) return;
		actionLoading = true;
		error = '';
		try {
			await adminApi.deleteUser(user.id);
			success = 'User deleted successfully';
			await loadUsers(usersData?.number || 0);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete user';
		} finally {
			actionLoading = false;
		}
	}

	async function handleDeletePhoto(photo: Photo) {
		if (!confirm('Are you sure you want to delete this photo?')) return;
		actionLoading = true;
		error = '';
		try {
			await adminApi.deletePhoto(photo.id);
			success = 'Photo deleted successfully';
			await loadPhotos(photosData?.number || 0);
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to delete photo';
		} finally {
			actionLoading = false;
		}
	}

	function formatDate(dateString: string | null): string {
		if (!dateString) return 'Never';
		return new Date(dateString).toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			year: 'numeric',
			hour: 'numeric',
			minute: '2-digit'
		});
	}
</script>

<svelte:head>
	<title>Admin Dashboard - InstaPound</title>
</svelte:head>

{#if loading}
	<div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
		<Spinner size="lg" />
	</div>
{:else if !authStore.isAdmin}
	<div class="flex min-h-[calc(100vh-200px)] items-center justify-center">
		<Card variant="elevated" padding="lg" class="text-center">
			<Shield class="mx-auto mb-4 h-12 w-12 text-red-500" />
			<h2 class="mb-2 text-xl font-semibold text-slate-900 dark:text-white">Access Denied</h2>
			<p class="mb-4 text-slate-600 dark:text-slate-400">You don't have permission to access this page.</p>
			<a href="/">
				<Button>Go Home</Button>
			</a>
		</Card>
	</div>
{:else}
	<div class="mx-auto max-w-7xl px-4 py-8">
		<!-- Header -->
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-slate-900 dark:text-white">Admin Dashboard</h1>
			<p class="mt-1 text-slate-600 dark:text-slate-400">Manage users, photos, and view system activity</p>
		</div>

		<!-- Alerts -->
		{#if success}
			<Alert variant="success" class="mb-6" dismissible>
				{success}
			</Alert>
		{/if}

		{#if error}
			<Alert variant="error" class="mb-6" dismissible>
				{error}
			</Alert>
		{/if}

		<!-- Tabs -->
		<div class="mb-6 flex gap-1 rounded-lg bg-slate-100 p-1 dark:bg-slate-800">
			<button
				onclick={() => handleTabChange('users')}
				class="flex flex-1 items-center justify-center gap-2 rounded-md px-4 py-2.5 text-sm font-medium transition-colors
				{activeTab === 'users'
					? 'bg-white text-slate-900 shadow dark:bg-slate-700 dark:text-white'
					: 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'}"
			>
				<Users class="h-4 w-4" />
				Users
			</button>
			<button
				onclick={() => handleTabChange('photos')}
				class="flex flex-1 items-center justify-center gap-2 rounded-md px-4 py-2.5 text-sm font-medium transition-colors
				{activeTab === 'photos'
					? 'bg-white text-slate-900 shadow dark:bg-slate-700 dark:text-white'
					: 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'}"
			>
				<Camera class="h-4 w-4" />
				Photos
			</button>
			<button
				onclick={() => handleTabChange('logs')}
				class="flex flex-1 items-center justify-center gap-2 rounded-md px-4 py-2.5 text-sm font-medium transition-colors
				{activeTab === 'logs'
					? 'bg-white text-slate-900 shadow dark:bg-slate-700 dark:text-white'
					: 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white'}"
			>
				<Activity class="h-4 w-4" />
				Activity Logs
			</button>
		</div>

		<!-- Content -->
		{#if activeTab === 'users'}
			<Card variant="elevated" padding="none">
				<div class="overflow-x-auto">
					<table class="w-full">
						<thead>
							<tr class="border-b border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800">
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">User</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Role</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Package</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Joined</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Last Login</th>
								<th class="px-6 py-4 text-right text-sm font-medium text-slate-500">Actions</th>
							</tr>
						</thead>
						<tbody>
							{#if usersData}
								{#each usersData.content as user}
									<tr class="border-b border-slate-100 dark:border-slate-800">
										<td class="px-6 py-4">
											<div class="flex items-center gap-3">
												<div class="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-indigo-500 text-sm font-semibold text-white">
													{user.username.charAt(0).toUpperCase()}
												</div>
												<div>
													<p class="font-medium text-slate-900 dark:text-white">{user.username}</p>
													<p class="text-sm text-slate-500">{user.email}</p>
												</div>
											</div>
										</td>
										<td class="px-6 py-4">
											<span class="rounded-full px-2.5 py-1 text-xs font-medium
											{user.role === 'ADMIN' ? 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-400' : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300'}">
												{user.role}
											</span>
										</td>
										<td class="px-6 py-4">
											<span class="rounded-full px-2.5 py-1 text-xs font-medium
											{user.packageType === 'GOLD' ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/50 dark:text-amber-400' : 
											 user.packageType === 'PRO' ? 'bg-violet-100 text-violet-700 dark:bg-violet-900/50 dark:text-violet-400' :
											 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300'}">
												{user.packageType}
											</span>
										</td>
										<td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
											{formatDate(user.createdAt)}
										</td>
										<td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
											{formatDate(user.lastLoginAt)}
										</td>
										<td class="px-6 py-4">
											<div class="flex justify-end gap-2">
												<button
													onclick={() => goto(`/admin/users/${user.id}`)}
													class="rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800"
													title="View details"
												>
													<Eye class="h-4 w-4" />
												</button>
												<button
													onclick={() => openEditUser(user)}
													class="rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800"
													title="Edit user"
												>
													<Edit class="h-4 w-4" />
												</button>
												<button
													onclick={() => handleDeleteUser(user)}
													disabled={user.id === authStore.user?.id}
													class="rounded-lg p-2 text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600 disabled:opacity-50 dark:hover:bg-red-900/50"
													title="Delete user"
												>
													<Trash2 class="h-4 w-4" />
												</button>
											</div>
										</td>
									</tr>
								{/each}
							{/if}
						</tbody>
					</table>
				</div>
				{#if usersData && usersData.totalPages > 1}
					<div class="border-t border-slate-200 p-4 dark:border-slate-700">
						<Pagination
							currentPage={usersData.number}
							totalPages={usersData.totalPages}
							onPageChange={loadUsers}
						/>
					</div>
				{/if}
			</Card>
		{:else if activeTab === 'photos'}
			{#if photosData}
				<div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
					{#each photosData.content as photo}
						<Card variant="elevated" padding="none" class="overflow-hidden">
							<div class="aspect-square bg-slate-100 dark:bg-slate-800">
								<img
									src={photo.thumbnailUrl || photo.imageUrl}
									alt={photo.description || photo.originalFileName}
									class="h-full w-full object-cover"
								/>
							</div>
							<div class="p-4">
								<p class="mb-1 text-sm font-medium text-slate-900 dark:text-white">{photo.author}</p>
								<p class="mb-3 text-xs text-slate-500">{formatDate(photo.uploadedAt)}</p>
								<div class="flex gap-2">
									<a href="/photo/{photo.id}" class="flex-1">
										<Button variant="secondary" size="sm" class="w-full">
											<Eye class="h-4 w-4" />
											View
										</Button>
									</a>
									<Button variant="danger" size="sm" onclick={() => handleDeletePhoto(photo)}>
										<Trash2 class="h-4 w-4" />
									</Button>
								</div>
							</div>
						</Card>
					{/each}
				</div>
				{#if photosData.totalPages > 1}
					<div class="mt-8">
						<Pagination
							currentPage={photosData.number}
							totalPages={photosData.totalPages}
							onPageChange={loadPhotos}
						/>
					</div>
				{/if}
			{/if}
		{:else if activeTab === 'logs'}
			<Card variant="elevated" padding="none">
				<div class="overflow-x-auto">
					<table class="w-full">
						<thead>
							<tr class="border-b border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800">
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Time</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">User</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Action</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">Description</th>
								<th class="px-6 py-4 text-left text-sm font-medium text-slate-500">IP Address</th>
							</tr>
						</thead>
						<tbody>
							{#if logsData}
								{#each logsData.content as log}
									<tr class="border-b border-slate-100 dark:border-slate-800">
										<td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
											{formatDate(log.timestamp)}
										</td>
										<td class="px-6 py-4 text-sm text-slate-900 dark:text-white">
											{log.username || 'Anonymous'}
										</td>
										<td class="px-6 py-4">
											<span class="rounded-full bg-violet-100 px-2.5 py-1 text-xs font-medium text-violet-700 dark:bg-violet-900/50 dark:text-violet-400">
												{log.actionType}
											</span>
										</td>
										<td class="max-w-xs truncate px-6 py-4 text-sm text-slate-600 dark:text-slate-400">
											{log.description}
										</td>
										<td class="px-6 py-4 text-sm text-slate-500">
											{log.ipAddress}
										</td>
									</tr>
								{/each}
							{/if}
						</tbody>
					</table>
				</div>
				{#if logsData && logsData.totalPages > 1}
					<div class="border-t border-slate-200 p-4 dark:border-slate-700">
						<Pagination
							currentPage={logsData.number}
							totalPages={logsData.totalPages}
							onPageChange={loadLogs}
						/>
					</div>
				{/if}
			</Card>
		{/if}
	</div>
{/if}

<!-- Edit User Modal -->
<Modal bind:open={showEditUserModal} title="Edit User" size="md">
	<div class="space-y-4">
		<Input
			label="Username"
			bind:value={editUsername}
			required
		/>
		<Input
			type="email"
			label="Email"
			bind:value={editEmail}
			required
		/>
		<Select
			label="Role"
			bind:value={editRole}
			options={roleOptions}
		/>
		<Select
			label="Package"
			bind:value={editPackage}
			options={packageOptions}
		/>
	</div>
	<div class="mt-6 flex gap-3">
		<Button variant="secondary" class="flex-1" onclick={() => (showEditUserModal = false)}>
			Cancel
		</Button>
		<Button class="flex-1" loading={actionLoading} onclick={handleUpdateUser}>
			Save Changes
		</Button>
	</div>
</Modal>
