package com.softgyan.findcallers.database.query;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.softgyan.findcallers.database.spam.SpamContract;
import com.softgyan.findcallers.models.BlockNumberModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public final class SpamQuery {
    private static final String TAG = "SpamQuery";
    public static final String BLOCK_NUMBER_LIST = "blockNumberList";
    public static final String IS_PRESENT = "isPresent";

    public synchronized static int insertBlockList(@Nullable Context context, final BlockNumberModel blockListModel) {
        if (context == null || blockListModel == null) {
            return 0;
        }

        boolean isPresent = false;
        final HashMap<String, Object> blockList = getBlockList(context, blockListModel.getNumber());
        final Object o = blockList.get(IS_PRESENT);
        if (o != null) {
            isPresent = (boolean) o;
        }

        if (isPresent) {
            return -1;
        }

        ContentValues values = new ContentValues();

        if (blockListModel.getNumber() != null) {
            values.put(SpamContract.BLOCK_COLUMN_NUMBER, blockListModel.getNumber());
        }

        if (blockListModel.getName() != null) {
            values.put(SpamContract.BLOCK_COLUMN_NAME, blockListModel.getName());
        }
        if (blockListModel.getType() == SpamContract.BLOCK_TYPE || blockListModel.getType() == SpamContract.SPAM_TYPE) {
            values.put(SpamContract.BLOCK_COLUMN_BLOCK_TYPE, blockListModel.getType());
        }


        final Uri insert = context.getContentResolver().insert(SpamContract.CONTENT_BLOCK_URI, values);
        final long l = ContentUris.parseId(insert);
        return (int) l;
    }

    public synchronized static int deleteSingleBlockList(@Nullable Context context, final int id) {
        if (context == null) {
            return 0;
        }
        return context.getContentResolver().delete(
                ContentUris.withAppendedId(SpamContract.CONTENT_BLOCK_URI, id),
                null,
                null
        );

    }

    public synchronized static int deleteAllBlockList(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        return context.getContentResolver().delete(
                SpamContract.CONTENT_BLOCK_URI,
                null,
                null
        );

    }

    public synchronized static HashMap<String, Object> getBlockList(@Nullable Context context,
                                                                    final String number) {
        if (context == null) {
            return null;
        }
        String selection = null;
        String[] selectionArgs = null;
        String sort = " ORDER BY " + SpamContract.BLOCK_COLUMN_NAME + " ASC";
        if (number != null) {
            selection = " WHERE " + SpamContract.BLOCK_COLUMN_NUMBER + " =?";
            selectionArgs = new String[]{number};
        }
        final Cursor query = context.getContentResolver().query(SpamContract.CONTENT_BLOCK_URI,
                null, selection, selectionArgs, sort);
        final int count = query.getCount();
        HashMap<String, Object> hashMap = new HashMap<>();
        if (count == 0) {
            hashMap.put(BLOCK_NUMBER_LIST, null);
            hashMap.put(IS_PRESENT, false);
        } else {
            List<BlockNumberModel> blockNumberModels = new ArrayList<>();
            while (query.moveToNext()) {
                BlockNumberModel model = new BlockNumberModel();
                model.setName(query.getString(query.getColumnIndex(SpamContract.BLOCK_COLUMN_NAME)));
                model.setNumber(query.getString(query.getColumnIndex(SpamContract.BLOCK_COLUMN_NUMBER)));
                model.setType(query.getInt(query.getColumnIndex(SpamContract.BLOCK_COLUMN_BLOCK_TYPE)));
                model.setId(query.getInt(query.getColumnIndex(SpamContract.BLOCK_ID)));

                blockNumberModels.add(model);
            }

            hashMap.put(BLOCK_NUMBER_LIST, blockNumberModels);
            hashMap.put(IS_PRESENT, true);
        }

        query.close();
        return hashMap;
    }

    public synchronized static List<BlockNumberModel> getBlockListArray(Context context, String number) {
        final HashMap<String, Object> blockList = getBlockList(context, number);
        final Object o = blockList.get(BLOCK_NUMBER_LIST);
        if (o != null)
            return (List<BlockNumberModel>) o;
        return null;
    }
}
