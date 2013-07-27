/*
   Copyright 2001 Nicholas Allen (nallen@freenet.co.uk)
   This file is part of JavaCVS.
   JavaCVS is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   JavaCVS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with JavaCVS; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package allensoft.diff;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;


/** Proceses differences and applies them to a Reader. The output is controlled by overridable
   printXXXX methods. By default these methods simply output the result of applying the
   differences to System.out. */
public class DiffProcessor {
    private BufferedReader m_In;
    private DiffParser m_DiffParser;

    /** Creates a new DiffProcessor that applies the differnces returned from <code>diffParser</code>
       to <code>file1</code>. */
    public DiffProcessor(Reader file1, DiffParser diffParser) {
        m_In = new BufferedReader(file1);
        m_DiffParser = diffParser;
    }

    /** Runs this DiffProcessor. */
    public void run() throws IOException, DiffException {
        int nLineNumInFile1 = 1;
        int nLineNumInFile2 = 1;
        String sLine;

        while(true) {
            Difference d = m_DiffParser.getNextDifference();

            if(d == null) {
                while((sLine = m_In.readLine()) != null) {
                    printLineInFile1(nLineNumInFile1, DiffType.NONE, sLine);
                    printLineInFile2(nLineNumInFile2, DiffType.NONE, sLine);
                    nLineNumInFile1++;
                    nLineNumInFile2++;
                }

                break;
            }

            if(d instanceof Insertion) {
                Insertion insertion = (Insertion)d;

                while(nLineNumInFile1 <= insertion.getStartLineInFile1()) {
                    sLine = m_In.readLine();

                    if(sLine == null)
                        throw new EOFException();

                    printLineInFile1(nLineNumInFile1, DiffType.NONE, sLine);
                    printLineInFile2(nLineNumInFile2, DiffType.NONE, sLine);
                    nLineNumInFile1++;
                    nLineNumInFile2++;
                }

                nLineNumInFile2 = insertion.getStartLineInFile2();

                String sInsertedText = insertion.getInsertedText();
                int i = 0;

                while(i < sInsertedText.length()) {
                    int nStartOfNextLine = sInsertedText.indexOf('\n', i);
                    sLine = (nStartOfNextLine == -1)
                        ? sInsertedText.substring(i)
                        : sInsertedText.substring(i, nStartOfNextLine);

                    printNonExistantLineInFile1();
                    printLineInFile2(nLineNumInFile2, DiffType.INSERTION, sLine);
                    nLineNumInFile2++;

                    if(nStartOfNextLine == -1)
                        break;

                    i = nStartOfNextLine + 1;
                }
            } else if(d instanceof Change) {
                Change change = (Change)d;

                while(nLineNumInFile1 < change.getStartLineInFile1()) {
                    sLine = m_In.readLine();

                    if(sLine == null)
                        throw new EOFException();

                    printLineInFile1(nLineNumInFile1, DiffType.NONE, sLine);
                    printLineInFile2(nLineNumInFile2, DiffType.NONE, sLine);
                    nLineNumInFile1++;
                    nLineNumInFile2++;
                }

                int nNumLinesPrintedInFile1 = 0;
                int nNumLinesPrintedInFile2 = 0;

                while(nLineNumInFile1 <= change.getEndLineInFile1()) {
                    sLine = m_In.readLine();

                    if(sLine == null)
                        throw new EOFException();

                    printLineInFile1(nLineNumInFile1, DiffType.CHANGE, sLine);
                    nLineNumInFile1++;
                    nNumLinesPrintedInFile1++;
                }

                nLineNumInFile2 = change.getStartLineInFile2();

                String sChangedText = change.getToText();
                int i = 0;

                while(i < sChangedText.length()) {
                    int nStartOfNextLine = sChangedText.indexOf('\n', i);
                    sLine = (nStartOfNextLine == -1)
                        ? sChangedText.substring(i)
                        : sChangedText.substring(i, nStartOfNextLine);

                    printLineInFile2(nLineNumInFile2, DiffType.CHANGE, sLine);
                    nLineNumInFile2++;
                    nNumLinesPrintedInFile2++;

                    if(nStartOfNextLine == -1)
                        break;

                    i = nStartOfNextLine + 1;
                }

                if(nNumLinesPrintedInFile1 > nNumLinesPrintedInFile2) {
                    for(int j = nNumLinesPrintedInFile2;
                            j < nNumLinesPrintedInFile1; j++)
                        printNonExistantLineInFile2();
                } else if(nNumLinesPrintedInFile2 > nNumLinesPrintedInFile1) {
                    for(int j = nNumLinesPrintedInFile1;
                            j < nNumLinesPrintedInFile2; j++)
                        printNonExistantLineInFile1();
                }
            } else if(d instanceof Deletion) {
                Deletion deletion = (Deletion)d;

                while(nLineNumInFile1 < deletion.getStartLineInFile1()) {
                    sLine = m_In.readLine();

                    if(sLine == null)
                        throw new EOFException();

                    printLineInFile1(nLineNumInFile1, DiffType.NONE, sLine);
                    printLineInFile2(nLineNumInFile2, DiffType.NONE, sLine);
                    nLineNumInFile1++;
                    nLineNumInFile2++;
                }

                while(nLineNumInFile1 <= deletion.getEndLineInFile1()) {
                    sLine = m_In.readLine();

                    if(sLine == null)
                        throw new EOFException();

                    printLineInFile1(nLineNumInFile1, DiffType.DELETION, sLine);
                    printNonExistantLineInFile2();
                    nLineNumInFile1++;
                }
            }
        }
    }

    /** Displays a line in File1. File1 is the file provided by the Reader supplied at construction time. */
    protected void printLineInFile1(int nLineNum, DiffType type, String sLine) {
    }

    /** Displays a non-existant line in File1. File1 is the file provided by the Reader supplied at construction time. */
    protected void printNonExistantLineInFile1() {
    }

    /** Displays a line in File2. File2 is the file resulting from applying the differences to File1. */
    protected void printLineInFile2(int nLineNum, DiffType type, String sLine) {
        System.out.println(sLine);
    }

    /** Displays a non-existant line in File2. File2 is the file resulting from applying the differences to File1. */
    protected void printNonExistantLineInFile2() {
    }
}
