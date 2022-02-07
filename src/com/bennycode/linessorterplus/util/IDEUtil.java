package com.bennycode.linessorterplus.util;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import java.util.ArrayList;
import java.util.List;

public class IDEUtil {

  public static String getFileName(Editor editor) {
    Document currentDoc = FileEditorManager.getInstance(editor.getProject()).getSelectedTextEditor().getDocument();
    VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
    return currentFile != null ? currentFile.getName() : new String();
  }

  public static List<String> extractLines(Document doc, int startLine, int endLine) {
    List<String> lines = new ArrayList<>(endLine - startLine);

    for (int i = startLine; i <= endLine; i++) {
      String line = IDEUtil.extractLine(doc, i);
      lines.add(line);
    }

    return lines;
  }

  private static String extractLine(Document doc, int lineNumber) {
    int lineSeparatorLength = doc.getLineSeparatorLength(lineNumber);
    int startOffset = doc.getLineStartOffset(lineNumber);
    int endOffset = doc.getLineEndOffset(lineNumber) + lineSeparatorLength;

    String line = doc.getCharsSequence().subSequence(startOffset, endOffset).toString();

    // If last line has no \n, add it one
    // This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
    // with \n... This is fixed after.
    if (lineSeparatorLength == 0) {
      line += "\n";
    }

    return line;
  }

  public static int ignoreLastEmptyLines(Document doc, int endLine) {
    while (endLine >= 0) {
      if (doc.getLineEndOffset(endLine) > doc.getLineStartOffset(endLine)) {
        return endLine;
      }

      endLine--;
    }

    return -1;
  }

  public static void writeStringToIDE(StringBuilder text, Document doc, int startLine, int endLine) {
    int startOffset = doc.getLineStartOffset(startLine);
    int endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine);
    doc.replaceString(startOffset, endOffset, text);
  }

  public static String reformatJson(String sortedJson, Project project) {
    if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
      return sortedJson;
    }

    if (CommandProcessor.getInstance().isUndoTransparentActionInProgress()) {
      return sortedJson;
    }

    PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText(JsonLanguage.INSTANCE, sortedJson);
    CodeStyleManager.getInstance(project).reformat(psiFile);
    return PsiDocumentManager.getInstance(project).getDocument(psiFile).getText();
  }
}
