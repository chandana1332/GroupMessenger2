package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int prior1 = 1;
    static final int prior2 = 2;
    static final int prior3 = 3;
    static final int prior4 = 4;
    static final int prior5 = 5;
    Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    static int universalcount = 0;
    static int count = 0;
    static int unicount = 0;
    private static final int TEST_CNT = 1;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    static final String[] ports = new String[]{"11108", "11112", "11116", "11120", "11124"};

    private static String msg;
    private static String portNumber;
    private static int sequenceNumber;
    private static final String Tproposed = "Proposed";
    private static final String Tagreed = "Agreed";
    private static final String Tnew = "New";
    private static String messageType;
    private static String messageId;
    private static String messageport;
    static String myPort = "";
    private static int proposedSequence = 0;
    private static int agreedSequence = 0;

    static Map<String, Map<String, Integer>> portcount = new HashMap<String, Map<String, Integer>>();
    static Map<Double, String> store = new HashMap<Double, String>();
    static Comparator<QueueObject> comparator = new agreedSeqComparator();
    static PriorityQueue<QueueObject> queue
            = new PriorityQueue<QueueObject>(100, comparator);
    List<Double> list = new ArrayList<Double>();

    int id = 0;

    static final int SERVER_PORT = 10000;
    static final String TAG = "Group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        System.out.println(myPort);
        try {
            /*
             e.printStackTrace();
             }


             final EditText editText = (EditText) findViewById(R.id.editText1);

             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = editText.getText().toString();
                editText.setText(""); // This is one way to reset the input box.

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, "", myPort, Tnew);

            }

        });

    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            try {
                while (true) {

                    Socket s = serverSocket.accept();
                    String message = "";
                    InputStreamReader ireader = new InputStreamReader(s.getInputStream());
                    BufferedReader breader = new BufferedReader(ireader);
                    message = breader.readLine();
                    messageId = breader.readLine();
                    messageport = breader.readLine();
                    messageType = breader.readLine();
                    if (messageType.equalsIgnoreCase(Tnew)) {
                        String mpt = breader.readLine();
                        String mcnt = breader.readLine();

                        proposedSequence = Integer.parseInt(mcnt);
                        proposedSequence = Math.max(proposedSequence, agreedSequence) + 1;

                        QueueObject ob1 = new QueueObject();
                        ob1.message = message;
                        ob1.messageId = messageId;
                        ob1.marked = false;
                        ob1.agreedSeq = 0.0;
                        ob1.messagePort = messageport;

                        queue.add(ob1);
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, messageId, messageport, Tproposed, Integer.toString(proposedSequence), myPort);

                    } else if (messageType.equalsIgnoreCase(Tproposed)) {

                        try {

                            int maxnum = 0;
                            Boolean res = false;

                            int prop = Integer.parseInt(breader.readLine());
                            String mport = breader.readLine();
                            Map<String, Integer> messageValue;
                            if (!portcount.containsKey(messageId)) {

                                messageValue = new HashMap<String, Integer>();
                                messageValue.put(messageport, prop);
                                portcount.put(messageId, messageValue);

                            } else {

                                messageValue = portcount.get(messageId);
                                if (!messageValue.containsKey(messageport)) {
                                    messageValue.put(messageport, prop);
                                    portcount.put(messageId, messageValue);
                                }

                            }

                            if (portcount.containsKey(messageId)) {
                                if (portcount.get(messageId).size() == ports.length) {
                                    res = true;
                                }
                            }

                            if (res) {
                                for (String name : portcount.keySet()) {

                                    String key = name.toString();

                                    Map<String, Integer> mval;

                                    mval = portcount.get(name);
                                    for (String i : mval.keySet()) {
                                        String k = i.toString();
                                        int val = mval.get(k);

                                    }

                                }

                                Map<String, Integer> value = portcount.get(messageId);

                                for (String portNumber : value.keySet()) {
                                    if (value.get(portNumber) > maxnum) {
                                        maxnum = value.get(portNumber);

                                    }
                                }

                                if (value != null) {
                                    portcount.remove(messageId);
                                }

                                agreedSequence = Math.max(agreedSequence, maxnum);
                                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, messageId, myPort, Tagreed, Integer.toString(agreedSequence));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (messageType.equalsIgnoreCase(Tagreed)) {
                        try {

                            int ag = Integer.parseInt(breader.readLine());

                            Double d = 0.0;

                            for (QueueObject o : queue) {
                                if (o != null) {
                                    if (o.messageId.equalsIgnoreCase(messageId)) {

                                        if (o.messagePort.equalsIgnoreCase(REMOTE_PORT0)) {
                                            d = ag + 0.1;
                                        } else if (o.messagePort.equalsIgnoreCase(REMOTE_PORT1)) {
                                            d = ag + 0.2;
                                        } else if (o.messagePort.equalsIgnoreCase(REMOTE_PORT2)) {
                                            d = ag + 0.3;
                                        } else if (o.messagePort.equalsIgnoreCase(REMOTE_PORT3)) {
                                            d = ag + 0.4;
                                        } else if (o.messagePort.equalsIgnoreCase(REMOTE_PORT4)) {
                                            d = ag + 0.5;
                                        }

                                        o.agreedSeq = d;
                                        o.marked = true;
                                    }
                                }

                            }
                            QueueObject dum = new QueueObject();
                            dum.agreedSeq = 0.0;
                            dum.marked = false;
                            dum.message = "";
                            dum.messageId = "";
                            dum.messagePort = "";
                            queue.add(dum);
                            queue.remove(dum);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        for (QueueObject qq : queue) {
                            if (qq != null) {
                            }

                        }

                        if (agreedSequence >= 20) {
                            Thread.sleep(1500);

                            for (int i = 0; i < queue.size(); i++) {
                                Double min = 100000000000.0;
                                QueueObject mi = null;
                                Iterator itr = queue.iterator();
                                while (itr.hasNext()) {
                                    QueueObject qw = (QueueObject) itr.next();
                                    if (qw.marked) {
                                        if (qw.agreedSeq < min && qw.agreedSeq > 0.0) {

                                            mi = qw;
                                            min = qw.agreedSeq;
                                        }
                                    }
                                }
                                if (mi != null) {
                                    publishProgress(mi.message);
                                    queue.remove(mi);
                                }
                            }
                        }

                        if (!queue.isEmpty()) {

                            for (QueueObject qq : queue) {
                                if (qq != null) {
                                } else {
                                    break;
                                }

                            }
                        }
                    }
                    ireader.close();
                    s.close();

                }

            } catch (InterruptedIOException iioe) {
                System.err.println("Remote host timed out during read operation");
                iioe.printStackTrace();

            } // Exception thrown when general network I/O error occurs
            catch (IOException ioe) {
                System.err.println("Network I/O error - " + ioe);
                ioe.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception is:" + e);
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            //TextView localTextView = (TextView) findViewById(R.id.textView1);
            //localTextView.append("\n");

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */ try {
                ContentValues[] cv = new ContentValues[TEST_CNT];
                ContentResolver mContentResolver = getContentResolver();
                for (int i = 0; i < TEST_CNT; i++) {
                    cv[i] = new ContentValues();
                    cv[i].put(KEY_FIELD, Integer.toString(universalcount++));
                    cv[i].put(VALUE_FIELD, strReceived);
                    mContentResolver.insert(uri, cv[0]);

                }

            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    public class checkfail {

        public void isAlive() {
            try {
                for (int i = 0; i < 5; i++) {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i]));

                    PrintWriter out = new PrintWriter(socket.getOutputStream());

                    out.println("");
                    out.println(count);
                    out.println(ports[i]);
                    out.println("a");
                    out.flush();

                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
                e.printStackTrace();
            }
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String msgToSend = msgs[0];
                String mid = msgs[1];

                String mport = msgs[2];
                String mtype = msgs[3];

                if (mtype.equalsIgnoreCase(Tnew)) {

                    count++;
                    Thread.sleep(500);
                    for (int i = 0; i < 5; i++) {

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i]));

                        PrintWriter out = new PrintWriter(socket.getOutputStream());

                        out.println(msgToSend);
                        out.println(count);
                        out.println(mport);
                        out.println(Tnew);
                        out.println(mport);
                        out.println(id++);
                        out.flush();

                        socket.close();
                    }

                } else if (mtype.equalsIgnoreCase(Tproposed)) {

                    try {
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(mport));

                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.println(msgToSend);
                        out.println(mid);
                        out.println(myPort);
                        out.println(Tproposed);
                        out.println(msgs[4]);
                        out.println(myPort);

                        out.flush();
                        socket.close();
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e);

                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e);

                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        System.out.println(e);

                        e.printStackTrace();
                    }

                } else if (mtype.equalsIgnoreCase(Tagreed)) {
                    for (int i = 0; i < 5; i++) {

                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(ports[i]));

                        PrintWriter out = new PrintWriter(socket.getOutputStream());

                        out.println(msgToSend);
                        out.println(mid);
                        out.println(myPort);
                        out.println(Tagreed);
                        out.println(msgs[4]);
                        out.flush();
                        socket.close();
                    }

                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
