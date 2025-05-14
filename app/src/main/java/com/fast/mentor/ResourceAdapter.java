package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.Resource;
import com.fast.mentor.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying lesson resources in a RecyclerView.
 */
public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private final Context context;
    private final List<Resource> resources = new ArrayList<>();
    private final DownloadManager downloadManager;

    /**
     * Constructor
     */
    public ResourceAdapter(Context context) {
        this.context = context;
        this.downloadManager = new DownloadManager(context);
    }

    /**
     * Set resources list and notify adapter
     */
    public void setResources(List<Resource> resources) {
        this.resources.clear();
        if (resources != null) {
            this.resources.addAll(resources);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        Resource resource = resources.get(position);
        holder.bind(resource);
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    /**
     * ViewHolder for resource items
     */
    class ResourceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView resourceIconImageView;
        private final TextView resourceTitleTextView;
        private final TextView resourceInfoTextView;
        private final ImageButton downloadButton;

        ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            resourceIconImageView = itemView.findViewById(R.id.resourceIconImageView);
            resourceTitleTextView = itemView.findViewById(R.id.resourceTitleTextView);
            resourceInfoTextView = itemView.findViewById(R.id.resourceInfoTextView);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }

        /**
         * Bind resource data to view
         */
        void bind(Resource resource) {
            resourceTitleTextView.setText(resource.getTitle());
            
            // Set resource info (type and size)
            String type = getResourceTypeString(resource.getType());
            String size = resource.getSize() > 0 ? 
                    FileUtils.formatFileSize(resource.getSize()) : "";
            
            if (!size.isEmpty()) {
                resourceInfoTextView.setText(String.format("%s • %s", type, size));
            } else {
                resourceInfoTextView.setText(type);
            }
            
            // Set icon based on resource type
            resourceIconImageView.setImageResource(getResourceTypeIcon(resource.getType()));
            
            // Set click listener for the whole item
            itemView.setOnClickListener(v -> openResource(resource));
            
            // Set click listener for download button
            downloadButton.setOnClickListener(v -> downloadResource(resource));
        }

        /**
         * Get resource type display string
         */
        private String getResourceTypeString(String type) {
            switch (type) {
                case Resource.TYPE_PDF:
                    return "PDF";
                case Resource.TYPE_VIDEO:
                    return "Video";
                case Resource.TYPE_AUDIO:
                    return "Audio";
                case Resource.TYPE_IMAGE:
                    return "Image";
                case Resource.TYPE_DOC:
                    return "Document";
                case Resource.TYPE_CODE:
                    return "Code";
                case Resource.TYPE_LINK:
                    return "Link";
                default:
                    return "File";
            }
        }

        /**
         * Get resource icon based on type
         */
        private int getResourceTypeIcon(String type) {
            switch (type) {
                case Resource.TYPE_PDF:
                    return R.drawable.ic_document;
                case Resource.TYPE_VIDEO:
                    return R.drawable.ic_video;
                case Resource.TYPE_AUDIO:
                    return R.drawable.ic_audio;
                case Resource.TYPE_IMAGE:
                    return R.drawable.ic_image;
                case Resource.TYPE_DOC:
                    return R.drawable.ic_document;
                case Resource.TYPE_CODE:
                    return R.drawable.ic_code;
                case Resource.TYPE_LINK:
                    return R.drawable.ic_code;
                default:
                    return R.drawable.ic_file;
            }
        }

        /**
         * Open resource based on type
         */
        private void openResource(Resource resource) {
            if (Resource.TYPE_LINK.equals(resource.getType())) {
                // Open link in browser
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resource.getUrl()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, R.string.unable_to_open_link, Toast.LENGTH_SHORT).show();
                }
            } else {
                // For other types, check if already downloaded, otherwise start download
                if (downloadManager.isResourceDownloaded(resource)) {
                    openDownloadedResource(resource);
                } else {
                    downloadResource(resource);
                }
            }
        }

        /**
         * Download resource
         */
        private void downloadResource(Resource resource) {
            // Start download
            downloadManager.downloadResource(resource, new DownloadManager.DownloadCallback() {
                @Override
                public void onDownloadStarted() {
                    Toast.makeText(context, R.string.download_started, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onDownloadProgress(int progress) {
                    // Could show progress here if needed
                }
                
                @Override
                public void onDownloadComplete(Uri fileUri) {
                    Toast.makeText(context, R.string.download_complete, Toast.LENGTH_SHORT).show();
                    openDownloadedResource(resource);
                }
                
                @Override
                public void onDownloadFailed(Exception error) {
                    Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * Open downloaded resource with appropriate viewer
         */
        private void openDownloadedResource(Resource resource) {
            Uri fileUri = downloadManager.getResourceFileUri(resource);
            if (fileUri == null) {
                Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, getMimeType(resource.getType()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Get MIME type based on resource type
         */
        private String getMimeType(String type) {
            switch (type) {
                case Resource.TYPE_PDF:
                    return "application/pdf";
                case Resource.TYPE_VIDEO:
                    return "video/*";
                case Resource.TYPE_AUDIO:
                    return "audio/*";
                case Resource.TYPE_IMAGE:
                    return "image/*";
                case Resource.TYPE_DOC:
                    return "application/msword";
                case Resource.TYPE_CODE:
                    return "text/plain";
                default:
                    return "*/*";
            }
        }
    }
}