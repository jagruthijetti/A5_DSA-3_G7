import React, { useState } from 'react';
import { Layers, Zap, HardDrive } from 'lucide-react';

export default function MaxHeapTree({ heapArray, onDispatch }) {
  const [viewMode, setViewMode] = useState('tree');

  if (!heapArray || heapArray.length === 0) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center text-slate-500 mb-6">
        <Layers className="w-10 h-10 mx-auto mb-2 text-slate-600 animate-bounce" />
        <p className="text-sm">Priority Queue (Binary Max-Heap) is currently empty.</p>
        <p className="text-xs text-slate-600 mt-1">Ingest a message above to populate heap nodes.</p>
      </div>
    );
  }

  const rootNode = heapArray[0];

  // Organize heap array into 2D levels: [[0], [1, 2], [3, 4, 5, 6], ...]
  const buildLevels = () => {
    const levels = [];
    let levelIndex = 0;
    let count = 1;

    while (levelIndex < heapArray.length) {
      levels.push(heapArray.slice(levelIndex, levelIndex + count));
      levelIndex += count;
      count *= 2;
    }
    return levels;
  };

  const levels = buildLevels();
  const totalLevels = levels.length;

  // Calculate dynamic auto-zoom scale based on total tree depth
  const zoomScale = Math.max(0.65, 1 - (totalLevels - 1) * 0.12);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 mb-6 shadow-2xl">
      {/* Header Bar */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 mb-5">
        <div>
          <div className="flex items-center gap-2">
            <Layers className="w-5 h-5 text-amber-400" />
            <h3 className="text-lg font-bold text-slate-100">Stage 2 & 3: Complete Binary Max-Heap Priority Tree</h3>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Strict Invariant: Parent Urgency ≥ Children Urgency | Dynamic Auto-Zoom Active
          </p>
        </div>

        <div className="flex items-center gap-2">
          {/* View Toggle */}
          <div className="bg-slate-950 p-1 rounded-lg border border-slate-800 flex text-xs">
            <button
              onClick={() => setViewMode('tree')}
              className={`px-3 py-1 rounded-md font-medium transition ${
                viewMode === 'tree' ? 'bg-slate-800 text-amber-300' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Tree View
            </button>
            <button
              onClick={() => setViewMode('array')}
              className={`px-3 py-1 rounded-md font-medium transition flex items-center gap-1 ${
                viewMode === 'array' ? 'bg-slate-800 text-amber-300' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <HardDrive className="w-3 h-3" />
              1D Array
            </button>
          </div>

          {/* O(1) Max Dispatch Button */}
          <button
            onClick={() => onDispatch(rootNode)}
            className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold px-4 py-2 rounded-lg flex items-center gap-2 transition shadow-lg shadow-amber-500/20 text-xs animate-pulse"
          >
            <Zap className="w-4 h-4 fill-current" />
            DISPATCH MAX PRIORITY (O(1))
          </button>
        </div>
      </div>

      {/* View Mode 1: Auto-Scaling Full Tree View */}
      {viewMode === 'tree' ? (
        <div className="bg-slate-950 p-6 rounded-xl border border-slate-800/80 overflow-x-auto min-h-[300px] flex flex-col items-center justify-center">
          <div 
            className="flex flex-col items-center gap-8 w-full transition-all duration-500 transform origin-top"
            style={{ transform: `scale(${zoomScale})` }}
          >
            {levels.map((levelNodes, levelIdx) => (
              <div key={levelIdx} className="flex justify-around items-center w-full gap-4">
                {levelNodes.map((node, nodeIdx) => {
                  const isRoot = levelIdx === 0 && nodeIdx === 0;

                  return isRoot ? (
                    /* ROOT NODE CARD: Shows detailed help message preview */
                    <div
                      key={node.id || nodeIdx}
                      className="bg-amber-950/40 border-2 border-amber-400 p-4 rounded-xl flex flex-col items-center text-center max-w-[300px] shadow-xl shadow-amber-500/10 transition-all hover:scale-105"
                    >
                      <span className="text-[10px] uppercase tracking-wider text-amber-400 font-bold">
                        ★ Root (Max Priority Target)
                      </span>
                      <span className="text-2xl font-black text-amber-300 font-mono my-1">
                        Score: {node.urgencyScore?.toFixed(2)}
                      </span>
                      <span className="text-xs font-semibold text-slate-200">
                        Target Node #{node.locationNodeId}
                      </span>
                      <p className="text-xs text-slate-300 italic mt-2 line-clamp-2 bg-slate-900/90 px-3 py-1.5 rounded border border-slate-800/80">
                        "{node.rawText}"
                      </p>
                    </div>
                  ) : (
                    /* CHILD NODES: Compact view without message preview */
                    <div
                      key={node.id || nodeIdx}
                      className="bg-slate-900/90 border border-slate-700/80 px-3 py-2 rounded-lg flex flex-col items-center text-center min-w-[90px] max-w-[130px] shadow-md transition-all hover:border-sky-500/50"
                    >
                      <span className="text-[9px] text-slate-500 font-mono">
                        N#{node.locationNodeId}
                      </span>
                      <span className="text-sm font-bold text-sky-400 font-mono">
                        {node.urgencyScore?.toFixed(2)}
                      </span>
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        </div>
      ) : (
        /* View Mode 2: 1D Memory Array Representation */
        <div className="bg-slate-950 p-6 rounded-xl border border-slate-800/80">
          <div className="flex gap-3 overflow-x-auto pb-2">
            {heapArray.map((node, i) => (
              <div
                key={i}
                className={`flex-shrink-0 p-3 rounded-lg border font-mono min-w-[120px] ${
                  i === 0
                    ? 'bg-amber-950/40 border-amber-500 text-amber-300'
                    : 'bg-slate-900 border-slate-800 text-slate-300'
                }`}
              >
                <div className="text-[10px] text-slate-500">Index [{i}]</div>
                <div className="text-base font-bold">Score: {node.urgencyScore?.toFixed(2)}</div>
                <div className="text-xs text-slate-200">Node #{node.locationNodeId}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Heap Metrics Bar */}
      <div className="mt-4 pt-3 border-t border-slate-800 flex justify-between text-xs text-slate-400 font-mono">
        <span>Total Tree Nodes: <strong className="text-slate-200">{heapArray.length}</strong></span>
        <span>Tree Height: <strong className="text-slate-200">{totalLevels}</strong></span>
        <span>Re-heapify Time: <strong className="text-amber-400">O(log N)</strong></span>
      </div>
    </div>
  );
}