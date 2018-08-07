/*
 * Copyright (c) 2018 Taner Sener
 *
 * This file is part of MobileFFmpeg.
 *
 * MobileFFmpeg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobileFFmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with MobileFFmpeg.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arthenica.mobileffmpeg.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Log;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.RunCallback;

import java.util.concurrent.Callable;

public class CommandTabFragment extends Fragment {

    private Context context;
    private EditText commandText;
    private TextView outputText;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() != null) {
            commandText = getView().findViewById(R.id.commandText);
            MainActivity.registerTooltipOnTouch(context, commandText, Tooltip.COMMAND_TEST_TOOLTIP_TEXT);

            // SET OUTPUT TEXT COLOR
            outputText = getView().findViewById(R.id.outputText);
            outputText.setMovementMethod(new ScrollingMovementMethod());

            View runButton = getView().findViewById(R.id.runButton);
            runButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    runFFmpeg();
                }
            });

            View runAsyncButton = getView().findViewById(R.id.runAsyncButton);
            runAsyncButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    runFFmpegAsync();
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setActive();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static CommandTabFragment newInstance(final Context context) {
        final CommandTabFragment fragment = new CommandTabFragment();
        fragment.setContext(context);
        return fragment;
    }

    public void enableLogCallback() {
        Log.enableLogCallback(new LogCallback() {

            @Override
            public void apply(final Log.Message message) {
                MainActivity.addUIAction(new Callable() {

                    @Override
                    public Object call() {
                        appendLog(message.getText());
                        return null;
                    }
                });
            }
        });
    }

    public void runFFmpeg() {
        String command = commandText.getText().toString();
        String[] ffmpegCommand = command.split(" ");

        clearLog();

        android.util.Log.d(MainActivity.TAG, "Testing COMMAND synchronously.");

        android.util.Log.d(MainActivity.TAG, String.format("FFmpeg process started with arguments \'%s\'", command));

        int result = FFmpeg.execute(ffmpegCommand);

        android.util.Log.d(MainActivity.TAG, String.format("FFmpeg process exited with rc %d", result));

        if (result != 0) {
            Popup.show(context, "Command failed. Please check output for the details.");
        }
    }

    public void runFFmpegAsync() {
        String command = commandText.getText().toString();
        String[] arguments = command.split(" ");

        clearLog();

        android.util.Log.d(MainActivity.TAG, "Testing COMMAND asynchronously.");

        android.util.Log.d(MainActivity.TAG, String.format("FFmpeg process started with arguments \'%s\'", command));

        MainActivity.executeAsync(new RunCallback() {

            @Override
            public void apply(int result) {

                android.util.Log.d(MainActivity.TAG, String.format("FFmpeg process exited with rc %d", result));

                if (result != 0) {
                    MainActivity.addUIAction(new Callable() {
                        @Override
                        public Object call() {
                            Popup.show(context, "Command failed. Please check output for the details.");
                            return null;
                        }
                    });
                }

            }
        }, arguments);
    }

    public void setActive() {
        android.util.Log.i(MainActivity.TAG, "Command Tab Activated");
        enableLogCallback();
    }

    public void appendLog(final String logMessage) {
        outputText.append(logMessage);
    }

    public void clearLog() {
        outputText.setText("");
    }

}
