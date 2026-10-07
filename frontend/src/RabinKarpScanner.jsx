import React from 'react';
import { ShieldCheck, ShieldAlert, Cpu, Hash } from 'lucide-react';

export default function RabinKarpScanner({ latestStreamItem, hashHistory }) {
  if (!latestStreamItem) {
    return (
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 mb-6 text-center text-slate-500">
        <Cpu className="w-8 h-8 mx-auto mb-2 text-slate-600 animate-pulse" />
        <p className="text-sm">Polynomial Rolling Hash Scanner Idle. Submit a message above to start stream deduplication.</p>
      </div>
    );
  }

  const { rawText, hashValue, isDuplicate, urgencyScore } = latestStreamItem;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 mb-6 shadow-2xl">
      <div className="flex justify-between items-center mb-4">
        <div className="flex items-center gap-2">
          <Hash className="w-5 h-5 text-indigo-400" />
          <h3 className="text-lg font-bold text-slate-100">Stage 1: Rabin-Karp Stream Deduplication Engine</h3>
        </div>
        <span className="text-xs font-mono bg-indigo-950 text-indigo-300 border border-indigo-800/50 px-3 py-1 rounded-full">
          Complexity: O(1) Rolling Window
        </span>
      </div>

      {/* Live Scanner Card */}
      <div className={`p-4 rounded-lg border transition-all ${
        isDuplicate 
          ? 'bg-rose-950/30 border-rose-500/50 text-rose-200' 
          : 'bg-emerald-950/30 border-emerald-500/50 text-emerald-200'
      }`}>
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              {isDuplicate ? (
                <span className="flex items-center gap-1 text-xs font-bold text-rose-400 bg-rose-900/50 px-2 py-0.5 rounded border border-rose-700/50">
                  <ShieldAlert className="w-3.5 h-3.5" /> DUPLICATE REJECTED
                </span>
              ) : (
                <span className="flex items-center gap-1 text-xs font-bold text-emerald-400 bg-emerald-900/50 px-2 py-0.5 rounded border border-emerald-700/50">
                  <ShieldCheck className="w-3.5 h-3.5" /> UNIQUE STREAM ITEM
                </span>
              )}
              <span className="text-xs font-mono text-slate-400">Hash: <span className="text-amber-300 font-bold">{hashValue}</span></span>
            </div>
            <p className="text-sm italic font-medium text-slate-100">"{rawText}"</p>
          </div>

          {!isDuplicate && (
            <div className="flex items-center gap-3 bg-slate-950/80 px-4 py-2 rounded-lg border border-slate-800">
              <span className="text-xs text-slate-400">Computed Urgency:</span>
              <span className="text-lg font-extrabold text-sky-400 font-mono">
                {urgencyScore ? urgencyScore.toFixed(2) : '0.00'}
              </span>
            </div>
          )}
        </div>

        {/* DSA Math Formula Explanation Callout */}
        <div className="mt-3 pt-3 border-t border-slate-800/80 text-xs font-mono text-slate-400 flex justify-between items-center">
          <span>Formula: H = (Σ c_i * b^(k-1-i)) mod p</span>
          <span className="text-slate-500">Hash Set Size: {hashHistory.length}</span>
        </div>
      </div>
    </div>
  );
}