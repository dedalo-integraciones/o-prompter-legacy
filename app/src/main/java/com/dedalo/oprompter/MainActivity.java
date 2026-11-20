package com.dedalo.oprompter;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.KeyListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private ScrollView scrollView;
    private Button buttonAdvance;
    private EditText editTextPrompter;
    private TextView textViewTooltip;
    private TextView textViewFileName;
    private KeyListener savedKeyListener;
    private boolean editMode = false;
    private MenuItem editMenuItem;
    private String currentFileName = "";
    private String rawTextWithSymbols = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateTitle();

        scrollView = findViewById(R.id.scrollViewPrompter);
        buttonAdvance = findViewById(R.id.buttonAdvance);
        editTextPrompter = findViewById(R.id.editTextPrompter);
        textViewTooltip = findViewById(R.id.textViewTooltip);
        textViewFileName = findViewById(R.id.textViewFileName);
        Button buttonPrevious = findViewById(R.id.buttonPrevious);
        Button buttonNext = findViewById(R.id.buttonNext);

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateFile(false);
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateFile(true);
            }
        });

        savedKeyListener = editTextPrompter.getKeyListener();
        editTextPrompter.setKeyListener(null);
        editTextPrompter.setFocusable(false);
        editTextPrompter.setFocusableInTouchMode(false);

        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "OPrompter");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception ignored) {
        }

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (!editMode) {
                    advanceScroll();
                    return true;
                }
                return false;
            }
        });

        View.OnTouchListener prompterTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!editMode) {
                    boolean handled = gestureDetector.onTouchEvent(event);
                    if (handled) {
                        return true;
                    }
                }
                return false;
            }
        };

        editTextPrompter.setOnTouchListener(prompterTouchListener);
        scrollView.setOnTouchListener(prompterTouchListener);

        buttonAdvance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advanceScroll();
            }
        });

        buttonAdvance.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setTag(true);
                Toast.makeText(MainActivity.this, "Modo arrastre activado", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        buttonAdvance.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Boolean.TRUE.equals(v.getTag())) {
                            v.setX(event.getRawX() + dX);
                            v.setY(event.getRawY() + dY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setTag(false);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        editMenuItem = menu.findItem(R.id.action_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_load) {
            showLoadDialog();
            return true;
        } else if (id == R.id.action_load_external) {
            openExternalFilePicker();
            return true;
        } else if (id == R.id.action_edit) {
            toggleEditMode();
            return true;
        } else if (id == R.id.action_save) {
            showSaveDialog();
            return true;
        } else if (id == R.id.action_exit) {
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openExternalFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Seleccionar archivo de texto"), PICK_FILE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No se encontró un explorador de archivos. Instala uno desde la Play Store.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            loadTextFromUri(uri);
        }
    }

    private void loadTextFromUri(Uri uri) {
        StringBuilder content = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            rawTextWithSymbols = content.toString();

            // Intentar obtener el nombre del archivo del Uri
            String fileName = "archivo_externo";
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
            currentFileName = fileName.replace(".txt", "");

            SpannableString formattedText = MarkdownParser.parse(rawTextWithSymbols);
            editTextPrompter.setText(formattedText);
            updateTitle();

            scrollView.scrollTo(0, 0);
            Toast.makeText(this, "Archivo cargado: " + currentFileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar el archivo", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (editMode) {
            editTextPrompter.setFocusable(true);
            editTextPrompter.setFocusableInTouchMode(true);
            editTextPrompter.setText(rawTextWithSymbols);
            editTextPrompter.setKeyListener(savedKeyListener);
            editTextPrompter.requestFocus();
            editTextPrompter.setSelection(editTextPrompter.getText().length());
            imm.showSoftInput(editTextPrompter, InputMethodManager.SHOW_IMPLICIT);
            textViewTooltip.setVisibility(View.VISIBLE);
            if (editMenuItem != null) editMenuItem.setTitle("Terminar Edición");
            Toast.makeText(this, "Modo edición: usa * _ ~ +", Toast.LENGTH_SHORT).show();
        } else {
            rawTextWithSymbols = editTextPrompter.getText().toString();
            SpannableString formattedText = MarkdownParser.parse(rawTextWithSymbols);
            editTextPrompter.setText(formattedText);
            editTextPrompter.setKeyListener(null);
            editTextPrompter.setFocusable(false);
            editTextPrompter.setFocusableInTouchMode(false);
            imm.hideSoftInputFromWindow(editTextPrompter.getWindowToken(), 0);
            textViewTooltip.setVisibility(View.GONE);
            if (editMenuItem != null) editMenuItem.setTitle("Editar Texto");
            Toast.makeText(this, "Modo prompter activado", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> getSortedOPrompterFiles() {
        File dir = new File(Environment.getExternalStorageDirectory(), "OPrompter");
        ArrayList<String> fileList = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().toLowerCase().endsWith(".txt")) {
                        fileList.add(f.getName());
                    }
                }
            }
        }
        Collections.sort(fileList, String.CASE_INSENSITIVE_ORDER);
        return fileList;
    }

    private void navigateFile(boolean isNext) {
        ArrayList<String> fileList = getSortedOPrompterFiles();

        if (fileList.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin archivos")
                    .setMessage("No se encontraron archivos .txt en la carpeta OPrompter.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }

        int currentIndex = -1;
        if (!currentFileName.isEmpty()) {
            String targetName = currentFileName.toLowerCase().endsWith(".txt") ? currentFileName : currentFileName + ".txt";
            for (int i = 0; i < fileList.size(); i++) {
                if (fileList.get(i).equalsIgnoreCase(targetName)) {
                    currentIndex = i;
                    break;
                }
            }
        }

        if (isNext) {
            if (currentIndex == -1) {
                loadTextFromFile(fileList.get(0));
            } else if (currentIndex < fileList.size() - 1) {
                loadTextFromFile(fileList.get(currentIndex + 1));
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Fin de la lista")
                        .setMessage("No hay más archivos disponibles después de este.")
                        .setPositiveButton("Aceptar", null)
                        .show();
            }
        } else {
            if (currentIndex > 0) {
                loadTextFromFile(fileList.get(currentIndex - 1));
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Inicio de la lista")
                        .setMessage("No hay archivos anteriores disponibles.")
                        .setPositiveButton("Aceptar", null)
                        .show();
            }
        }
    }

    private void showLoadDialog() {
        final ArrayList<String> names = getSortedOPrompterFiles();
        if (names.isEmpty()) {
            Toast.makeText(this, "No hay archivos .txt en OPrompter", Toast.LENGTH_SHORT).show();
            return;
        }
        final CharSequence[] items = names.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("Cargar archivo")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loadTextFromFile(items[which].toString());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadTextFromFile(String fileName) {
        File file = new File(new File(Environment.getExternalStorageDirectory(), "OPrompter"), fileName);
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            reader.close();

            rawTextWithSymbols = content.toString();
            currentFileName = fileName.replace(".txt", "");

            SpannableString formattedText = MarkdownParser.parse(rawTextWithSymbols);
            editTextPrompter.setText(formattedText);
            updateTitle();

            scrollView.scrollTo(0, 0);
            Toast.makeText(this, "Archivo cargado: " + currentFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar", Toast.LENGTH_LONG).show();
        }
    }

    private void showSaveDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nombre del archivo");
        if (!currentFileName.isEmpty()) {
            input.setText(currentFileName);
        }
        new AlertDialog.Builder(this)
                .setTitle("Guardar archivo")
                .setMessage("Escribe el nombre:")
                .setView(input)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String fileName = input.getText().toString().trim();
                        if (!fileName.isEmpty()) {
                            saveTextToFile(fileName, rawTextWithSymbols);
                            currentFileName = fileName;
                            updateTitle();
                        } else {
                            Toast.makeText(MainActivity.this, "Nombre vacío", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void saveTextToFile(String fileName, String content) {
        File dir = new File(Environment.getExternalStorageDirectory(), "OPrompter");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Guardado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show();
        }
    }

    private void advanceScroll() {
        if (scrollView != null) {
            int scrollAmount = scrollView.getHeight();
            scrollView.smoothScrollBy(0, scrollAmount);
        }
    }

    private void updateTitle() {
        if (!currentFileName.isEmpty()) {
            setTitle("O'Prompter [" + currentFileName + "]");
            if (textViewFileName != null) {
                textViewFileName.setText(currentFileName);
            }
        } else {
            setTitle("O'Prompter");
            if (textViewFileName != null) {
                textViewFileName.setText("O'Prompter");
            }
        }
    }
}