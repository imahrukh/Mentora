package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.SubmissionFile;
import com.fast.mentor.FileUtils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adapter for displaying submission files in a RecyclerView.
 */
public class SubmissionFileAdapter extends RecyclerView.Adapter<SubmissionFileAdapter.FileViewHolder> {

    private Context context;
    private List<SubmissionFile> files = new ArrayList<>();
    private boolean editable;
    private OnFileRemovedListener onFileRemovedListener;
    private FirebaseStorage storage;

    /**
     * Interface for file removal callback
     */
    public interface OnFileRemovedListener {
        void onFileRemoved(int position);
    }

    /**
     * Constructor
     */
    public SubmissionFileAdapter(Context context, boolean editable, OnFileRemovedListener listener) {
        this.context = context;
        this.editable = editable;
        this.onFileRemovedListener = listener;
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Set files
     */
    public void setFiles(List<SubmissionFile> files) {
        this.files = files != null ? files : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Get files
     */
    public List<SubmissionFile> getFiles() {
        return files;
    }

    /**
     * Add file from URI
     */
    public void addFileUri(Uri uri) {
        // Get file name
        String fileName = getFileNameFromUri(uri);

        // Create submission file
        SubmissionFile file = new SubmissionFile();
        file.setId(UUID.randomUUID().toString());
        file.setName(fileName);
        file.setLocalUri(uri.toString());
        file.setType(getFileType(uri));

        // Add to list
        files.add(file);

        // Notify adapter
        notifyItemInserted(files.size() - 1);
    }

    /**
     * Get file name from URI
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;

        // Try to get display name from content resolver
        if (uri.getScheme().equals("content")) {
            String[] projection = {android.provider.MediaStore.MediaColumns.DISPLAY_NAME};
            try (android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
                    result = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                // Fallback to path
            }
        }

        // If not found, try to get last path segment
        if (result == null) {
            result = uri.getLastPathSegment();
        }

        // If still not found, use a default name
        if (result == null) {
            result = "file_" + UUID.randomUUID().toString().substring(0, 8);
        }

        return result;
    }

    /**
     * Get file type from URI
     */
    private String getFileType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);

        if (mimeType == null) {
            // Try to get from file extension
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (fileExtension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            }
        }

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("audio/")) {
                return "audio";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            } else if (mimeType.startsWith("text/")) {
                return "text";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            } else if (mimeType.contains("msword") || mimeType.contains("wordprocessingml")) {
                return "doc";
            } else if (mimeType.contains("spreadsheet") || mimeType.contains("excel")) {
                return "xls";
            } else if (mimeType.contains("presentation") || mimeType.contains("powerpoint")) {
                return "ppt";
            } else if (mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("tar") || mimeType.contains("compressed")) {
                return "archive";
            } else if (mimeType.contains("code") || mimeType.contains("java") || mimeType.contains("javascript") ||
                    mimeType.contains("python") || mimeType.contains("html") || mimeType.contains("css")) {
                return "code";
            }
        }

        // Default type
        return "file";
    }

    /**
     * Remove file at position
     */
    public void removeFile(int position) {
        if (position >= 0 && position < files.size()) {
            files.remove(position);
            notifyItemRemoved(position);

            if (onFileRemovedListener != null) {
                onFileRemovedListener.onFileRemoved(position);
            }
        }
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
        SubmissionFile file = files.get(position);

        // Set file name
        holder.fileNameTextView.setText(file.getName());

        // Set file type icon
        holder.fileTypeImageView.setImageResource(getFileTypeIcon(file.getType()));

        // Set visibility of remove button
        holder.removeButton.setVisibility(editable ? View.VISIBLE : View.GONE);

        // Set click listener for remove button
        holder.removeButton.setOnClickListener(v -> removeFile(holder.getAdapterPosition()));

        // Set click listener for file view
        holder.itemView.setOnClickListener(v -> {
            if (editable) return; // Don't open file in edit mode

            // Check if file has remote URL or local URI
            if (file.getUrl() != null && !file.getUrl().isEmpty()) {
                // Remote file needs to be downloaded first
                downloadAndOpenFile(file);
            } else if (file.getLocalUri() != null && !file.getLocalUri().isEmpty()) {
                // Local file can be opened directly
                try {
                    openFile(Uri.parse(file.getLocalUri()));
                } catch (Exception e) {
                    Toast.makeText(context, R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Reset progress visibility
        holder.progressBar.setVisibility(View.GONE);
    }

    /**
     * Download and open a file
     */
    private void downloadAndOpenFile(SubmissionFile file) {
        // Show downloading toast
        Toast.makeText(context, R.string.downloading_file_to_view, Toast.LENGTH_SHORT).show();

        // Get file reference
        StorageReference fileRef = storage.getReferenceFromUrl(file.getUrl());

        // Create local file
        try {
            File localFile = File.createTempFile("download_", getFileExtension(file.getName()));

            // Download file
            fileRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        // File downloaded successfully
                        Uri fileUri = FileProvider.getUriForFile(context,
                                context.getPackageName() + ".fileprovider",
                                localFile);

                        // Open file
                        try {
                            openFile(fileUri);
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failed download
                        Toast.makeText(context, "Error downloading file: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            Toast.makeText(context, "Error creating temp file: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open a file with an appropriate app
     */
    private void openFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, context.getContentResolver().getType(fileUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get file extension from name
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    /**
     * Get icon resource for file type
     */
    private int getFileTypeIcon(String type) {
        switch (type) {
            case "image":
                return R.drawable.ic_image;
            case "audio":
                return R.drawable.ic_audio;
            case "video":
                return R.drawable.ic_video;
            case "pdf":
                return R.drawable.ic_pdf;
            case "doc":
                return R.drawable.ic_doc;
            case "xls":
                return R.drawable.ic_xls;
            case "ppt":
                return R.drawable.ic_ppt;
            case "code":
                return R.drawable.ic_code;
            default:
                return R.drawable.ic_file;
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    /**
     * ViewHolder for file items
     */
    public static class FileViewHolder extends RecyclerView.ViewHolder {
        public ImageView fileTypeImageView;
        public TextView fileNameTextView;
        public ImageButton removeButton;
        public ProgressBar progressBar;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileTypeImageView = itemView.findViewById(R.id.fileTypeImageView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            removeButton = itemView.findViewById(R.id.removeButton);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}