import React, { useState } from 'react';
import { PackageCheck, Truck } from 'lucide-react';

export default function KnapsackLogistics({ targetNode }) {
  const [capacity, setCapacity] = useState(50); // Increased payload capacity

  const items = [
    { id: 1, name: 'Medical First Aid Kits', weight: 8, utility: 80 },
    { id: 2, name: 'Clean Drinking Water Pack', weight: 15, utility: 90 },
    { id: 3, name: 'High-Calorie Rations', weight: 10, utility: 60 },
    { id: 4, name: 'Emergency Shelter Tents', weight: 20, utility: 100 },
    { id: 5, name: 'Portable Oxygen Tank', weight: 12, utility: 85 },
    { id: 6, name: 'Power Generators', weight: 25, utility: 110 },
  ];

  const solveKnapsack = () => {
    const n = items.length;
    const dp = Array.from({ length: n + 1 }, () => Array(capacity + 1).fill(0));

    for (let i = 1; i <= n; i++) {
      const { weight, utility } = items[i - 1];
      for (let w = 0; w <= capacity; w++) {
        if (weight <= w) {
          dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weight] + utility);
        } else {
          dp[i][w] = dp[i - 1][w];
        }
      }
    }

    let w = capacity;
    const selected = [];
    for (let i = n; i > 0 && w > 0; i--) {
      if (dp[i][w] !== dp[i - 1][w]) {
        selected.push(items[i - 1]);
        w -= items[i - 1].weight;
      }
    }

    return {
      maxUtility: dp[n][capacity],
      selectedItems: selected,
      totalWeight: selected.reduce((sum, item) => sum + item.weight, 0),
    };
  };

  const result = solveKnapsack();

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 mb-6 shadow-2xl">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-3 mb-4">
        <div>
          <div className="flex items-center gap-2">
            <Truck className="w-5 h-5 text-sky-400" />
            <h3 className="text-lg font-bold text-slate-100">
              Stage 5: 0/1 Knapsack Supply Payload Optimizer
            </h3>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Maximizes Relief Utility under Payload Weight Limit $W$
          </p>
        </div>

        {/* Dynamic Slider up to 100kg */}
        <div className="flex items-center gap-3 bg-slate-950 px-4 py-2 rounded-lg border border-slate-800">
          <span className="text-xs font-mono text-slate-400">Truck Capacity:</span>
          <input
            type="range"
            min="10"
            max="100"
            step="5"
            value={capacity}
            onChange={(e) => setCapacity(parseInt(e.target.value, 10))}
            className="w-32 accent-sky-400 h-1.5 bg-slate-800 rounded cursor-pointer"
          />
          <span className="text-sm font-bold text-sky-400 font-mono">{capacity} kg</span>
        </div>
      </div>

      {targetNode && (
        <div className="mb-4 p-3 bg-sky-950/30 border border-sky-500/30 rounded-lg flex items-center justify-between text-xs">
          <span className="text-slate-300">
            Optimizing payload manifest for Target: <strong className="text-amber-300">Node #{targetNode.locationNodeId}</strong>
          </span>
          <span className="font-mono text-sky-400 font-bold">Urgency Score: {targetNode.urgencyScore?.toFixed(2)}</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-slate-950 p-4 rounded-lg border border-slate-800">
          <h4 className="text-xs font-bold font-mono text-slate-400 mb-2 uppercase">Available Supply Inventory</h4>
          <div className="space-y-1.5">
            {items.map((item) => {
              const isSelected = result.selectedItems.some((s) => s.id === item.id);
              return (
                <div
                  key={item.id}
                  className={`p-2 rounded text-xs flex justify-between items-center transition ${
                    isSelected
                      ? 'bg-sky-950/50 border border-sky-500/40 text-sky-200'
                      : 'bg-slate-900 border border-slate-800 text-slate-400'
                  }`}
                >
                  <span className="font-medium">{item.name}</span>
                  <span className="font-mono text-[11px]">
                    {item.weight} kg | Utility: {item.utility}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        <div className="bg-slate-950 p-4 rounded-lg border border-slate-800 flex flex-col justify-between">
          <div>
            <h4 className="text-xs font-bold font-mono text-emerald-400 mb-2 uppercase flex items-center gap-1.5">
              <PackageCheck className="w-4 h-4 text-emerald-400" />
              Optimal Truck Payload Manifest
            </h4>
            <div className="space-y-1.5">
              {result.selectedItems.map((item) => (
                <div key={item.id} className="p-2 bg-emerald-950/30 border border-emerald-500/30 rounded text-xs text-emerald-300 flex justify-between font-mono">
                  <span>✔ {item.name}</span>
                  <span>+{item.utility} Utility</span>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-800 flex justify-between items-center text-xs font-mono">
            <div>
              <span className="text-slate-400">Total Payload: </span>
              <span className="text-slate-200 font-bold">{result.totalWeight} / {capacity} kg</span>
            </div>
            <div>
              <span className="text-slate-400">Max Utility Value: </span>
              <span className="text-amber-400 font-extrabold text-sm">{result.maxUtility}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}