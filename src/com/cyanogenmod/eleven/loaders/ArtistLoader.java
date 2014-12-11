/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.cyanogenmod.eleven.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.ArtistColumns;

import com.cyanogenmod.eleven.model.Artist;
import com.cyanogenmod.eleven.sectionadapter.SectionCreator;
import com.cyanogenmod.eleven.utils.Lists;
import com.cyanogenmod.eleven.utils.PreferenceUtils;
import com.cyanogenmod.eleven.utils.SortOrder;
import com.cyanogenmod.eleven.utils.SortUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI} and
 * return the artists on a user's device.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ArtistLoader extends SectionCreator.SimpleListLoader<Artist> {

    /**
     * The result
     */
    private ArrayList<Artist> mArtistsList = Lists.newArrayList();

    /**
     * The {@link Cursor} used to run the query.
     */
    private Cursor mCursor;

    /**
     * Constructor of <code>ArtistLoader</code>
     * 
     * @param context The {@link Context} to use
     */
    public ArtistLoader(final Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Artist> loadInBackground() {
        // Create the Cursor
        mCursor = makeArtistCursor(getContext());
        // Gather the data
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                // Copy the artist id
                final long id = mCursor.getLong(0);

                // Copy the artist name
                final String artistName = mCursor.getString(1);

                // Copy the number of albums
                final int albumCount = mCursor.getInt(2);

                // Copy the number of songs
                final int songCount = mCursor.getInt(3);

                // as per designer's request, don't show unknown artist
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    continue;
                }

                // Create a new artist
                final Artist artist = new Artist(id, artistName, songCount, albumCount);

                mArtistsList.add(artist);
            } while (mCursor.moveToNext());
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        // requested artist ordering
        String artistSortOrder = PreferenceUtils.getInstance(mContext).getArtistSortOrder();
        // run a custom localized sort to try to fit items in to header buckets more nicely
        if (shouldEvokeCustomSortRoutine(artistSortOrder)) {
            mArtistsList = SortUtils.localizeSortList(mArtistsList, artistSortOrder);
        }

        return mArtistsList;
    }

    /**
     * Evoke custom sorting routine if the sorting attribute is a String. MediaProvider's sort
     * can be trusted in other instances
     * @param sortOrder
     * @return
     */
    private boolean shouldEvokeCustomSortRoutine(String sortOrder) {
        return sortOrder.equals(SortOrder.ArtistSortOrder.ARTIST_A_Z) ||
               sortOrder.equals(SortOrder.ArtistSortOrder.ARTIST_Z_A);
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     * 
     * @param context The {@link Context} to use.
     * @return The {@link Cursor} used to run the artist query.
     */
    public static final Cursor makeArtistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        ArtistColumns.ARTIST,
                        /* 2 */
                        ArtistColumns.NUMBER_OF_ALBUMS,
                        /* 3 */
                        ArtistColumns.NUMBER_OF_TRACKS
                }, null, null, PreferenceUtils.getInstance(context).getArtistSortOrder());
    }
}