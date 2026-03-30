KOMPRES — File Compression Tool
Data Structures Project  
Modi Meetkumar — 2025IMG-029	Arsh Jain — 2025IMG-007	Hitesh Chandra — 2025IMG-027
Repo link: https://github.com/meetcodz/Kompres
Problem Description
Large files waste storage and slow down data transfer. Kompres solves this by implementing two lossless compression algorithms — Huffman Coding and LZW — in a single desktop application, allowing any file to be compressed and perfectly restored without data loss.
Input Details
•	Any file type — text (.txt, .csv), source code, binary (.exe, .bin), XML, JSON, images, and more.
•	File selected via drag-and-drop or a file browser dialog in the GUI.
•	For decompression: a .huffz (Huffman) or .LmZWp (LZW) compressed file is provided as input.
Methodology Followed
Huffman Coding: 
•	Builds a frequency table by scanning every byte in the input, constructs a min-heap priority queue to build the Huffman tree — frequent bytes get shorter codes. The file stores the frequency table header, padding bits, and bit-packed data. Decompression rebuilds the tree from the header and decodes bit-by-bit.
LZW Algorithm: 
•	Pre-seeds a dictionary with all 256 byte values. A two-pass approach first determines the required bit-width, then compresses using variable-width codes. New multi-byte sequences are added on-the-fly with a 10 MB memory cap. Decompression rebuilds the same dictionary in order.
GUI: 
•	Built with PyQt6 — dark-themed drag-and-drop interface. Compression runs in a background QThread         keeping the UI responsive, with a results panel showing size, ratio, and time.
Output
•	Compression produces a .huffz or .LmZWp file in the same directory as the input.
•	GUI displays original size, compressed size, space saved (%), and time taken (ms) with an animated progress bar.
•	Decompression perfectly restores the original file; filename conflicts handled automatically (e.g. file(1).txt).
Novelty of the Work
•	Dual-algorithm in one tool: users can compare Huffman vs LZW on the same file and pick the best result.
•	Full binary safety: operates at raw byte level — works on any file type, not just ASCII text.
•	Non-blocking GUI with live stats: real-time compression ratio, space saved, and elapsed time.
