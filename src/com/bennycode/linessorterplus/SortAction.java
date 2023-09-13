package com.bennycode.linessorterplus;

import com.bennycode.linessorterplus.util.IDEUtil;
import com.bennycode.linessorterplus.util.JSONUtil;
import com.bennycode.linessorterplus.util.SortUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.project.Project;
import java.text.MessageFormat;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Basic action to sort plaintext and JSON files or selections. Based on the <a href="https://plugins.jetbrains.com/plugin/5919-lines-sorter">Lines Sorter Plugin</a> from Sylvain Francois and modified by <a href="https://twitter.com/bennycode">Benny Neugebauer</a> to support JSON sorting.
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain Francois</a>
 * @author <a href="https://bennycode.com/">Benny Neugebauer</a>
 */
public class SortAction extends TextComponentEditorAction {

  private static final Logger LOGGER = Logger.getInstance(SortAction.class);

  protected SortAction() {
    super(new Handler());
  }

  private static class Handler extends EditorWriteActionHandler {

    public void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext) {
      String fileName = IDEUtil.getFileName(editor);
      final Document doc = editor.getDocument();
      Project project = editor.getProject();

      boolean isSelection = editor.getSelectionModel().hasSelection();
      boolean isJsonFile = fileName.matches(".*\\.json[5c]?");

      final String parsingMode = isSelection ? "Selection Mode" : "File Mode";
      final String fileMode = isJsonFile ? "JSON file" : "Plaintext";

      LOGGER.debug(MessageFormat.format("Sorting file \"{0}\" ({1}, {2}).", fileName, fileMode, parsingMode));

      int startLine;
      int endLine;

      if (isSelection) {
        startLine = doc.getLineNumber(editor.getSelectionModel().getSelectionStart());
        endLine = doc.getLineNumber(editor.getSelectionModel().getSelectionEnd());
        if (doc.getLineStartOffset(endLine) == editor.getSelectionModel().getSelectionEnd()) {
          endLine--;
        }
      } else {
        startLine = 0;
        endLine = doc.getLineCount() - 1;
      }

      // Ignore last lines (usually one) which are only '\n'
      endLine = IDEUtil.ignoreLastEmptyLines(doc, endLine);

      // Extract text as a list of lines
      // TODO: Don't extract just lines but really take the selected characters into account, so that you can select a JSON object which starts from the same lines of code.
      // Example: `"scripts": {` <- here it should be possible to select everything starting from the curly bracket to sort the selected chars as JSON.
      List<String> lines = IDEUtil.extractLines(doc, startLine, endLine);

      StringBuilder sortedText;

      // Sort JSON files & selections
      if (isJsonFile == true && JSONUtil.isValidJSON(lines)) {
        LOGGER.debug("Applying JSON sorting...");
        sortedText = SortUtil.sortJson(lines);
        String formattedJson = IDEUtil.reformatJson(sortedText.toString(), project);
        IDEUtil.writeStringToIDE(new StringBuilder(formattedJson), doc, startLine, endLine);
        return;
      }

      // Sort regular texts
      LOGGER.debug("Applying plaintext sorting...");
      sortedText = SortUtil.sortText(lines);

      // Remove last \n (added by `extractLine`) if sort has been applied on whole file and the file did not end with \n
      if (!isSelection) {
        CharSequence charsSequence = doc.getCharsSequence();
        if (charsSequence.charAt(charsSequence.length() - 1) != '\n') {
          sortedText.deleteCharAt(sortedText.length() - 1);
        }
      }

      IDEUtil.writeStringToIDE(sortedText, doc, startLine, endLine);
    }
  }
}
