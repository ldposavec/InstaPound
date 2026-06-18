import type {
    Photo,
    PhotoSearchRequest,
    ImageFormat,
    ImageFilter,
    StorageType,
    PhotoEditRequest
} from "$lib/types";
import { photosApi, ApiError } from "$lib/api";

function createPhotosStore() {
    let photos = $state<Photo[]>([]);
    let currentPhoto = $state<Photo | null>(null);
    let loading = $state(false);
    let error = $state<string | null>(null);
    let page = $state(0);
    let totalPages = $state(0);
    let totalElements = $state(0);

    async function browse(pageNum = 0, size = 15) {
        loading = true;
        error = null;
        try {
            const result = await photosApi.browse(pageNum, size);
            photos = result.content;
            page = result.number;
            totalPages = result.totalPages;
            totalElements = result.totalElements;
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to load photos';
        } finally {
            loading = false;
        }
    }

    async function search(request: PhotoSearchRequest) {
        loading = true;
        error = null;
        try {
            const result = await photosApi.search(request);
            photos = result.content;
            page = result.number;
            totalPages = result.totalPages;
            totalElements = result.totalElements;
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Search failed';
        } finally {
            loading = false;
        }
    }

    async function getById(id: number) {
        loading = true;
        error = null;
        try {
            currentPhoto = await photosApi.getById(id);
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to get photo';
            currentPhoto = null;
        } finally {
            loading = false;
        }
    }

    async function upload(file: File, options: {
        description?: string;
        hashtags?: string[];
        storageType?: StorageType;
        format?: ImageFormat;
        width?: number;
        height?: number;
    }) : Promise<Photo> {
        loading = true;
        error = null;
        try {
            return await photosApi.upload(file, options);
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to upload photo';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function edit(id: number, data: PhotoEditRequest) {
        loading = true;
        error = null;
        try {
            const photo = await photosApi.edit(id, data);
            if (currentPhoto?.id === id) {
                currentPhoto = photo;
            }
            const index = photos.findIndex((p) => p.id === id);
            if (index !== -1) {
                photos[index] = photo
            }
            return photo;
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to edit photo';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function deletePhoto(id: number) {
        loading = true;
        error = null;
        try {
            await photosApi.delete(id);
            photos = photos.filter((p) => p.id !== id);
            if (currentPhoto?.id === id) {
                currentPhoto = null;
            }
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to delete photo';
            throw e;
        } finally {
            loading = false;
        }
    }

    async function download(id: number, options: {
        original?: boolean;
        format?: ImageFormat;
        width?: number;
        height?: number;
        filters?: ImageFilter[];
    } = { original: true }) {
        try {
            const blob = await photosApi.download(id, options);
            const photo = photos.find((p) => p.id === id) || currentPhoto;
            const filename = photo?.originalFilename || `photo-${id}`;
            const extension = options.format?.toLowerCase() || filename.split('.').pop() || 'jpg';
            const downloadName = filename.includes('.')
                ? filename.replace(/\.[^.]+$/, `.${extension}`) : `${filename}.${extension}`;
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = downloadName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        } catch (e) {
            error = e instanceof ApiError ? e.message : 'Failed to download photo';
            throw e;
        }
    }

    function clear() {
        photos = [];
        currentPhoto = null;
        error = null;
        page = 0;
        totalPages = 0;
        totalElements = 0;
    }

    return {
        get photos() {
            return photos;
        },
        get currentPhoto() {
            return currentPhoto;
        },
        get loading() {
            return loading;
        },
        get error() {
            return error;
        },
        get page() {
            return page;
        },
        get totalPages() {
            return totalPages;
        },
        get totalElements() {
            return totalElements;
        },
        browse,
        search,
        getById,
        upload,
        edit,
        deletePhoto,
        download
    };
}

export const photosStore = createPhotosStore();