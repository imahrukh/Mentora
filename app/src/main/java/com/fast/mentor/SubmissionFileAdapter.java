package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.model.SubmissionFile;
import com.fast.mentor.util.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying submission files in a RecyclerView.
 */
public class SubmissionFileAdapter extends RecyclerView.Adapter<SubmissionFileAdapter.FileViewHolder> {

    private final Context context;
    private final List<Object> items = new ArrayList<>(); // Can be Uri or SubmissionFile
    private final boolean isEditMode;
    private final SubmissionFileListener listener;

    /**
     * Interface for file operations
     */
    public interface SubmissionFileListener {
        void onRemoveFile(int position);
    }

    /**
     * Constructor
     */
    public SubmissionFileAdapter(Context context, boolean isEditMode, SubmissionFileListener listener) {
        this.context = context;
        this.isEditMode = isEditMode;
        this.listener = listener;
    }

    /**
     * Set existing submission files
     */
    public void setFiles(List<SubmissionFile> files) {
        items.clear();
        if (files != null) {
            items.addAll(files);
        }
        notifyDataSetChanged();
    }

    /**
     * Add a new file URI for upload
     */
    public void addFileUri(Uri uri) {
        items.add(uri);
        notifyItemInserted(items.size() - 1);
    }

    /**
     * Get all files (existing and new) as SubmissionFile objects
     */
    public List<SubmissionFile> getFiles() {
        List<SubmissionFile> result = new ArrayList<>();
        
        for (Object item : items) {
            if (item instanceof SubmissionFile) {
                result.add((SubmissionFile) item);
            } else if (item instanceof Uri) {
                // Create new SubmissionFile from Uri
                Uri uri = (Uri) item;
                String fileName = getFileNameFromUri(uri);
                String fileType = FileUtils.getResourceTypeFromExtension(fileName);
                
                SubmissionFile file = new SubmissionFile();
                file.setName(fileName);
                file.setType(fileType);
                file.setUri(uri.toString()); // Store local uri temporarily
                file.setLocal(true); // Mark as local file
                
                result.add(file);
            }
        }
        
        return result;
    }

    /**
     * Get filename from URI
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        
        return result != null ? result : "file";
    }

    /**
     * Get file size from URI
     */
    private long getFileSizeFromUri(Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return size;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        Object item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for submission files
     */
    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fileIconImageView;
        private final TextView fileNameTextView;
        private final TextView fileInfoTextView;
        private final ImageButton viewFileButton;
        private final ImageButton downloadFileButton;
        private final ImageButton removeFileButton;
        private final ProgressBar uploadProgressBar;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIconImageView = itemView.findViewById(R.id.fileIconImageView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            fileInfoTextView = itemView.findViewById(R.id.fileInfoTextView);
            viewFileButton = itemView.findViewById(R.id.viewFileButton);
            downloadFileButton = itemView.findViewById(R.id.downloadFileButton);
            removeFileButton = itemView.findViewById(R.id.removeFileButton);
            uploadProgressBar = itemView.findViewById(R.id.uploadProgressBar);
        }

        /**
         * Bind file data to view
         */
        void bind(Object item, int position) {
            // Setup based on item type
            if (item instanceof SubmissionFile) {
                bindSubmissionFile((SubmissionFile) item);
            } else if (item instanceof Uri) {
                bindFileUri((Uri) item);
            }
            
            // Show remove button only in edit mode
            removeFileButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            
            // Set click listener for remove button
            removeFileButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveFile(position);
                }
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size() - position);
            });
        }

        /**
         * Bind submission file data
         */
        private void bindSubmissionFile(SubmissionFile file) {
            fileNameTextView.setText(file.getName());
            
            // Set file info text
            StringBuilder infoBuilder = new StringBuilder();
            
            // Add file type
            String type = getDisplayFileType(file.getType());
            infoBuilder.append(type);
            
            // Add file size if available
            if (file.getSize() > 0) {
                infoBuilder.append(" • ").append(FileUtils.formatFileSize(file.getSize()));
            }
            
            // Add upload date if available
            Date uploadDate = file.getUploadedAt();
            if (uploadDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                infoBuilder.append(" • ").append(dateFormat.format(uploadDate));
            }
            
            fileInfoTextView.setText(infoBuilder.toString());
            
            // Set file type icon
            fileIconImageView.setImageResource(getFileTypeIcon(file.getType()));
            
            // Setup buttons
            if (file.isLocal()) {
                // Local file (not yet uploaded)
                uploadProgressBar.setVisibility(View.VISIBLE);
                viewFileButton.setOnClickListener(v -> openLocalFile(Uri.parse(file.getUri())));
                downloadFileButton.setVisibility(View.GONE);
            } else {
                // Remote file
                uploadProgressBar.setVisibility(View.GONE);
                viewFileButton.setOnClickListener(v -> viewRemoteFile(file));
                downloadFileButton.setOnClickListener(v -> downloadRemoteFile(file));
            }
        }

        /**
         * Bind file URI data
         */
        private void bindFileUri(Uri uri) {
            // Get file info
            String fileName = getFileNameFromUri(uri);
            long fileSize = getFileSizeFromUri(uri);
            String fileType = FileUtils.getResourceTypeFromExtension(fileName);
            
            // Set file name
            fileNameTextView.setText(fileName);
            
            // Set file info text
            String type = getDisplayFileType(fileType);
            String size = fileSize > 0 ? FileUtils.formatFileSize(fileSize) : "";
            
            if (!size.isEmpty()) {
                fileInfoTextView.setText(String.format("%s • %s", type, size));
            } else {
                fileInfoTextView.setText(type);
            }
            
            // Set file type icon
            fileIconImageView.setImageResource(getFileTypeIcon(fileType));
            
            // Setup buttons
            uploadProgressBar.setVisibility(View.GONE);
            viewFileButton.setOnClickListener(v -> openLocalFile(uri));
            downloadFileButton.setVisibility(View.GONE);
        }

        /**
         * Get display file type string
         */
        private String getDisplayFileType(String type) {
            switch (type) {
                case "pdf":
                    return "PDF";
                case "document":
                    return "Document";
                case "image":
                    return "Image";
                case "video":
                    return "Video";
                case "audio":
                    return "Audio";
                case "code":
                    return "Code";
                default:
                    return "File";
            }
        }

        /**
         * Get file type icon resource
         */
        private int getFileTypeIcon(String type) {
            switch (type) {
                case "pdf":
                    return R.drawable.ic_document;
                case "document":
                    return R.drawable.ic_document;
                case "image":
                    return R.drawable.ic_image;
                case "video":
                    return R.drawable.ic_video;
                case "audio":
                    return R.drawable.ic_audio;
                case "code":
                    return R.drawable.ic_code;
                default:
                    return R.drawable.ic_file;
            }
        }

        /**
         * Open local file
         */
        private void openLocalFile(Uri uri) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, context.getContentResolver().getType(uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * View remote file
         */
        private void viewRemoteFile(SubmissionFile file) {
            // In a real implementation, this would download the file first if not cached
            // For simplicity, we'll just show a toast message
            Toast.makeText(context, R.string.downloading_file_to_view, Toast.LENGTH_SHORT).show();
        }

        /**
         * Download remote file
         */
        private void downloadRemoteFile(SubmissionFile file) {
            // In a real implementation, this would download the file
            // For simplicity, we'll just show a toast message
            Toast.makeText(context, R.string.downloading_file, Toast.LENGTH_SHORT).show();
        }
    }
}