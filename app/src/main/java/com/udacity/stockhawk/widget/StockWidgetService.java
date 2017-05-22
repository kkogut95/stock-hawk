package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by kacper on 21/05/2017.
 */

public class StockWidgetService extends RemoteViewsService {

    private static final int INDEX_SYMBOL = 1;
    private static final int INDEX_PRICE = 2;
    private static final int INDEX_ABSOLUTE_CHANGE = 3;
    private static final int INDEX_PERCENTAGE_CHANGE = 4;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mStockData = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (mStockData != null)
                    mStockData.close();
                final long pId = Binder.clearCallingIdentity();
                mStockData = getContentResolver().query(
                        Contract.Quote.URI,
                        new String[]{Contract.Quote._ID,
                                Contract.Quote.COLUMN_SYMBOL,
                                Contract.Quote.COLUMN_PRICE,
                                Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                                Contract.Quote.COLUMN_PERCENTAGE_CHANGE},
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC"
                );
                Binder.restoreCallingIdentity(pId);
            }

            @Override
            public void onDestroy() {
                if (mStockData != null)
                    mStockData.close();
                mStockData = null;
            }

            @Override
            public int getCount() {
                if (mStockData == null)
                    return 0;
                else
                    return mStockData.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (AdapterView.INVALID_POSITION == position || mStockData == null || !mStockData.moveToPosition(position))
                    return null;
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_stock_item);
                String symbol = mStockData.getString(INDEX_SYMBOL);
                String price = "$"+mStockData.getString(INDEX_PRICE);
                String change = "";

                if (mStockData.getFloat(INDEX_ABSOLUTE_CHANGE) > 0) {
                    remoteViews.setTextColor(R.id.widget_percentage_change_tv, ContextCompat.getColor(getBaseContext(),R.color.material_green_700));
                    change += "+";
                } else {
                    remoteViews.setTextColor(R.id.widget_percentage_change_tv, ContextCompat.getColor(getBaseContext(),R.color.material_red_700));
                    change += "-";
                }
                change+=mStockData.getString(INDEX_PERCENTAGE_CHANGE)+"%";
                remoteViews.setTextViewText(R.id.symbol, symbol);
                remoteViews.setTextViewText(R.id.price, price);
                remoteViews.setTextViewText(R.id.widget_percentage_change_tv, change);

                Bundle extras = new Bundle();
                extras.putString("SYMBOL", symbol);

                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                remoteViews.setOnClickFillInIntent(R.id.list_item_ll, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_stock_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mStockData.moveToPosition(position))
                    return mStockData.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
