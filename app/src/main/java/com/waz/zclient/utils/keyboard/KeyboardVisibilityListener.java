/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.utils.keyboard;

import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import com.waz.zclient.utils.ContextUtils;

public class KeyboardVisibilityListener {
    private final View contentView;
    private final int statusAndNavigationBarHeight;
    private int keyboardHeight;

    public interface Callback {
        void onKeyboardChanged(boolean keyboardIsVisible, int keyboardHeight);
        void onKeyboardHeightChanged(int keyboardHeight);
    }
    private Callback callback;

    public KeyboardVisibilityListener(View contentView) {
        this.contentView = contentView;
        if (contentView == null) {
            this.statusAndNavigationBarHeight = 0;
            return;
        }
        this.statusAndNavigationBarHeight = ContextUtils.getNavigationBarHeight(contentView.getContext()) + ContextUtils.getStatusBarHeight(contentView.getContext());
    }

    public void setCallback(Callback keyboardCallback) {
        this.callback = keyboardCallback;
    }

    public int getKeyboardHeight() {
        return keyboardHeight;
    }

    public synchronized void onLayoutChange() {
        if (!layoutChangeInProgress) {
            layoutChangeInProgress = true;
            layoutChangeHandler.removeCallbacks(layoutChangeRunnable);
            layoutChangeHandler.postDelayed(layoutChangeRunnable, 200);
        }
    }

    private boolean layoutChangeInProgress = false;

    private Handler layoutChangeHandler = new Handler();

    private Runnable layoutChangeRunnable = new Runnable() {
        @Override
        public void run() {
            Rect r = new Rect();
            contentView.getWindowVisibleDisplayFrame(r);

            int newKeyboardHeight = Math.max(0, contentView.getRootView().getHeight() - r.bottom - statusAndNavigationBarHeight);

            if (newKeyboardHeight != keyboardHeight) {
                boolean visibilityChanged = keyboardHeight == 0 || newKeyboardHeight == 0;
                keyboardHeight = newKeyboardHeight;

                if (callback != null) {
                    callback.onKeyboardHeightChanged(newKeyboardHeight);
                    if (visibilityChanged) {
                        callback.onKeyboardChanged(newKeyboardHeight > 0, newKeyboardHeight);
                    }
                }
            }

            layoutChangeInProgress = false;
        }
    };
}
