/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bitcoin_glass;

import com.google.android.glass.widget.CardScrollAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Adapter for the {@link CardScrollView} inside {@link SetTimerActivity}.
 */
public class SetCurrencyScrollAdapter extends CardScrollAdapter {

    public enum CurrencyComponents {
        CURRENCIES(0, 10, R.string.currencies);

        private final int mPosition;
        private final int mMaxValue;
        private final int mLabelResourceId;

        CurrencyComponents(int pos, int max, int resId) {
            mPosition = pos;
            mMaxValue = max;
            mLabelResourceId = resId;
        }

        /**
         * Gets the component's position in the values array.
         */
        public int getPosition() {
            return mPosition;
        }

        /**
         * Gets the maximum value that this component can hold.
         */
        public int getMaxValue() {
            return mMaxValue;
        }

        /**
         * Gets the resource ID for the component's label.
         */
        public int getLabelResourceId() {
            return mLabelResourceId;
        }
    }

    private final Context mContext;
    private final long[] mValues;
    

    public SetCurrencyScrollAdapter(Context context) {
        mContext = context;
        mValues = new long[3];
    }

    /**
     * Sets the total duration in milliseconds.
     */
    public void setCurrencyValue(long cur) {
    	Log.i("bitcoin set Currency - "+ cur);
        mValues[CurrencyComponents.CURRENCIES.getPosition()] =cur;
    }

    /**
     * Get the total duration in milliseconds.
     */
    public long getCurrencyValue() {
    	Log.i("bitcoin get Currency - "+mValues[CurrencyComponents.CURRENCIES.getPosition()]);
        return mValues[CurrencyComponents.CURRENCIES.getPosition()];
    }

    /**
     * Set a specific time component value.
     */
    public void setCurrencyComponent(CurrencyComponents component, int value) {
        mValues[component.getPosition()] = value;
    }

    /**
     * Get a specific time component value.
     */
    public long getCurrencyComponent(CurrencyComponents component) {
        return mValues[component.getPosition()];
    }

    @Override
    public int getCount() {
        return mValues.length;
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < mValues.length) {
            return CurrencyComponents.values()[position];
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.bitcoinlivecard, parent);
        }
        final TextView[] views = new TextView[] {

            (TextView) convertView.findViewById(R.id.currencies)
        };
        final TextView tipView = (TextView) convertView.findViewById(R.id.tip);
        final String tipLabel = mContext.getResources().getString(
                ((CurrencyComponents) getItem(position)).getLabelResourceId());

        tipView.setText(tipLabel);

       
            views[0].setText(String.format("%02d", mValues[0]));
            views[0].setTextColor(mContext.getResources().getColor(R.color.gray));
        views[position].setTextColor(mContext.getResources().getColor(R.color.white));
Log.i("bitcoin - setCurAdapter:"+mValues[0]);
        return setItemOnCard(this, convertView);
    }

    @Override
    public int findIdPosition(Object id) {
        if (id instanceof CurrencyComponents) {
        	CurrencyComponents component = (CurrencyComponents) id;
            return component.getPosition();
        }
        return AdapterView.INVALID_POSITION;
    }

    @Override
    public int findItemPosition(Object item) {
        return findIdPosition(item);
    }
}
