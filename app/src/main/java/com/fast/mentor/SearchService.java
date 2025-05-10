package com.fast.mentor;

import androidx.annotation.NonNull;

import com.fast.mentor.model.SearchResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class to handle search operations with Firestore
 */
public class SearchService {

    private final FirebaseFirestore db;
    
    public interface SearchCallback {
        void onSearchResults(List<SearchResult> results);
        void onSearchError(Exception e);
    }
    
    public SearchService() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Search for courses, modules, or lessons based on query and filter
     * 
     * @param query The search query
     * @param filter Filter by result type or null for all types
     * @param callback Callback for search results or error
     */
    public void search(String query, SearchResult.ResultType filter, SearchCallback callback) {
        if (query == null || query.trim().length() < 2) {
            callback.onSearchResults(new ArrayList<>());
            return;
        }
        
        String queryLowerCase = query.toLowerCase().trim();
        List<Query> queries = buildQueries(queryLowerCase, filter);
        List<SearchResult> results = new ArrayList<>();
        
        // Execute all queries and collect results
        for (Query firestoreQuery : queries) {
            firestoreQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Get collection name to determine result type
                        String collectionName = document.getReference().getParent().getId();
                        SearchResult result = createSearchResultFromDocument(document, collectionName);
                        
                        if (result != null && (filter == null || result.getType() == filter)) {
                            // Avoid duplicates (could happen when searching by multiple fields)
                            if (!containsResultWithId(results, result.getId())) {
                                results.add(result);
                            }
                        }
                    }
                    
                    // Return results sorted by relevance (matched title first)
                    results.sort((a, b) -> {
                        boolean aContainsQuery = a.getTitle().toLowerCase().contains(queryLowerCase);
                        boolean bContainsQuery = b.getTitle().toLowerCase().contains(queryLowerCase);
                        
                        if (aContainsQuery && !bContainsQuery) return -1;
                        if (!aContainsQuery && bContainsQuery) return 1;
                        return 0;
                    });
                    
                    callback.onSearchResults(results);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onSearchError(e);
                }
            });
        }
    }
    
    /**
     * Build Firestore queries based on search term and filter
     */
    private List<Query> buildQueries(String query, SearchResult.ResultType filter) {
        List<Query> queries = new ArrayList<>();
        
        // Course search
        if (filter == null || filter == SearchResult.ResultType.COURSE) {
            // Search by title prefix
            queries.add(db.collection("courses")
                    .orderBy("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limit(20));
            
            // Search by category or description or keywords if they exist
            queries.add(db.collection("courses")
                    .whereArrayContains("keywords", query)
                    .limit(20));
        }
        
        // Module search
        if (filter == null || filter == SearchResult.ResultType.MODULE) {
            queries.add(db.collection("modules")
                    .orderBy("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limit(20));
        }
        
        // Lesson search
        if (filter == null || filter == SearchResult.ResultType.LESSON) {
            queries.add(db.collection("lessons")
                    .orderBy("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .limit(20));
        }
        
        return queries;
    }
    
    /**
     * Create a SearchResult object from a Firestore document
     */
    private SearchResult createSearchResultFromDocument(QueryDocumentSnapshot document, String collectionName) {
        try {
            String id = document.getId();
            String title = document.getString("title");
            
            // If title is missing, skip this document
            if (title == null) {
                return null;
            }
            
            String description = document.getString("description") != null ? 
                    document.getString("description") : "";
                    
            String imageUrl = document.getString("imageUrl");
            String authorName = document.getString("authorName") != null ? 
                    document.getString("authorName") : "Unknown";
                    
            String category = document.getString("category") != null ? 
                    document.getString("category") : "General";
            
            float rating = 0;
            if (document.contains("rating")) {
                Object ratingObj = document.get("rating");
                if (ratingObj instanceof Long) {
                    rating = ((Long) ratingObj).floatValue();
                } else if (ratingObj instanceof Double) {
                    rating = ((Double) ratingObj).floatValue();
                }
            }
            
            int lessonsCount = 0;
            if (document.contains("lessonsCount")) {
                Object lessonsObj = document.get("lessonsCount");
                if (lessonsObj instanceof Long) {
                    lessonsCount = ((Long) lessonsObj).intValue();
                } else if (lessonsObj instanceof Integer) {
                    lessonsCount = (Integer) lessonsObj;
                }
            }
            
            int durationMinutes = 0;
            if (document.contains("durationMinutes")) {
                Object durationObj = document.get("durationMinutes");
                if (durationObj instanceof Long) {
                    durationMinutes = ((Long) durationObj).intValue();
                } else if (durationObj instanceof Integer) {
                    durationMinutes = (Integer) durationObj;
                }
            }
            
            // Determine type based on collection name
            SearchResult.ResultType resultType;
            switch (collectionName) {
                case "courses":
                    resultType = SearchResult.ResultType.COURSE;
                    break;
                case "modules":
                    resultType = SearchResult.ResultType.MODULE;
                    break;
                case "lessons":
                    resultType = SearchResult.ResultType.LESSON;
                    break;
                case "resources":
                    resultType = SearchResult.ResultType.RESOURCE;
                    break;
                default:
                    // Unknown collection type
                    return null;
            }
            
            return new SearchResult(
                    id, 
                    title, 
                    description, 
                    resultType, 
                    imageUrl, 
                    authorName, 
                    category, 
                    rating, 
                    lessonsCount, 
                    durationMinutes);
                    
        } catch (Exception e) {
            // Log error and skip this result
            return null;
        }
    }
    
    /**
     * Check if results list already contains a result with the given id
     */
    private boolean containsResultWithId(List<SearchResult> results, String id) {
        for (SearchResult result : results) {
            if (result.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}