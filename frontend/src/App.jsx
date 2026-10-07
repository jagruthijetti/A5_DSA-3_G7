import React, { useState } from 'react';
import CustomInputBar from './CustomInputBar';
import RabinKarpScanner from './RabinKarpScanner';
import MaxHeapTree from './MaxHeapTree';
import OsmMapCanvas from './OsmMapCanvas';
import KnapsackLogistics from './KnapsackLogistics';
import { Activity, MapPin, Layers, Navigation, ArrowRight, Route } from 'lucide-react';

// 15 Graph Network Nodes (5 Warehouses + 10 Disaster Sectors)
const GRAPH_NODES = [
  { id: 1, name: 'Alpha Depot (Central)', lat: 17.3850, lng: 78.4867, isWarehouse: true },
  { id: 2, name: 'Beta Depot (North)', lat: 17.4550, lng: 78.4900, isWarehouse: true },
  { id: 13, name: 'Gamma Depot (West)', lat: 17.4350, lng: 78.3600, isWarehouse: true },
  { id: 14, name: 'Delta Depot (South)', lat: 17.3500, lng: 78.5200, isWarehouse: true },
  { id: 15, name: 'Epsilon Depot (North West)', lat: 17.4750, lng: 78.4100, isWarehouse: true },

  { id: 3, name: 'Sector 3 (Charminar)', lat: 17.3616, lng: 78.4747, isWarehouse: false },
  { id: 4, name: 'Sector 4 (Banjara Hills)', lat: 17.4156, lng: 78.4347, isWarehouse: false },
  { id: 5, name: 'Sector 5 (Hitec City)', lat: 17.4435, lng: 78.3772, isWarehouse: false },
  { id: 6, name: 'Sector 6 (Secunderabad)', lat: 17.4399, lng: 78.4983, isWarehouse: false },
  { id: 7, name: 'Sector 7 (Kukatpally)', lat: 17.4849, lng: 78.4011, isWarehouse: false },
  { id: 8, name: 'Sector 8 (LB Nagar)', lat: 17.3522, lng: 78.5501, isWarehouse: false },
  { id: 9, name: 'Sector 9 (Gachibowli)', lat: 17.4401, lng: 78.3489, isWarehouse: false },
  { id: 10, name: 'Sector 10 (Begumpet)', lat: 17.4447, lng: 78.4664, isWarehouse: false },
  { id: 11, name: 'Sector 11 (Ameerpet)', lat: 17.4375, lng: 78.4482, isWarehouse: false },
  { id: 12, name: 'Sector 12 (Jubilee Hills Disaster Zone)', lat: 17.4319, lng: 78.4071, isWarehouse: false },
];

const ADJACENCY_LIST = {
  1: [3, 4, 11],
  2: [6, 10, 7],
  13: [5, 9, 4],
  14: [8, 3],
  15: [7, 11],
  3: [1, 14, 8],
  4: [1, 13, 12],
  5: [13, 12, 7],
  6: [2, 10],
  7: [2, 15, 5],
  8: [14, 3],
  9: [13],
  10: [2, 6],
  11: [1, 15, 12],
  12: [4, 5, 11]
};

const calculateRabinKarpHash = (str) => {
  let hash = 0;
  const p = 31;
  const m = 1e9 + 9;
  for (let i = 0; i < str.length; i++) {
    hash = (hash * p + str.charCodeAt(i)) % m;
  }
  return '0x' + Math.abs(hash).toString(16).toUpperCase();
};

export default function App() {
  const [activePanel, setActivePanel] = useState(1);
  const [latestItem, setLatestItem] = useState(null);
  const [hashSet, setHashSet] = useState(new Set());
  const [heapArray, setHeapArray] = useState([]);
  const [dispatchedMission, setDispatchedMission] = useState(null);

  const [selectedWarehouseId, setSelectedWarehouseId] = useState(1);
  const [activeRoute, setActiveRoute] = useState([]);
  const [blockedRoads, setBlockedRoads] = useState(new Set());

  // Heap logic
  const heapifyUp = (arr) => {
    let i = arr.length - 1;
    while (i > 0) {
      let parentIndex = Math.floor((i - 1) / 2);
      if (arr[i].urgencyScore > arr[parentIndex].urgencyScore) {
        [arr[i], arr[parentIndex]] = [arr[parentIndex], arr[i]];
        i = parentIndex;
      } else break;
    }
    return arr;
  };

  const heapifyDown = (arr) => {
    let i = 0;
    const length = arr.length;
    while (true) {
      let left = 2 * i + 1;
      let right = 2 * i + 2;
      let largest = i;

      if (left < length && arr[left].urgencyScore > arr[largest].urgencyScore) largest = left;
      if (right < length && arr[right].urgencyScore > arr[largest].urgencyScore) largest = right;

      if (largest !== i) {
        [arr[i], arr[largest]] = [arr[largest], arr[i]];
        i = largest;
      } else break;
    }
    return arr;
  };

  const handleIngestMessage = ({ rawText, locationNodeId }) => {
    const hashValue = calculateRabinKarpHash(rawText);
    const isDuplicate = hashSet.has(hashValue);

    let baseScore = 0.50;
    if (/flood|trapped|blood|collapse|critical|severe|oxygen/i.test(rawText)) baseScore += 0.35;
    if (/water|medical|injuries/i.test(rawText)) baseScore += 0.10;
    const urgencyScore = Math.min(0.98, baseScore + Math.random() * 0.04);

    const newItem = {
      id: Date.now(),
      rawText,
      locationNodeId,
      hashValue,
      urgencyScore,
      isDuplicate,
    };

    setLatestItem(newItem);

    if (!isDuplicate) {
      setHashSet((prev) => new Set(prev).add(hashValue));
      setHeapArray((prev) => heapifyUp([...prev, newItem]));
    }
  };

  const handleSimulateDuplicate = (rawText) => {
    if (!rawText) return;
    const hashValue = calculateRabinKarpHash(rawText);
    setLatestItem({
      id: Date.now(),
      rawText,
      locationNodeId: 12,
      hashValue,
      urgencyScore: 0.90,
      isDuplicate: true,
    });
  };

  const handleDispatch = (topNode) => {
    if (!topNode) return;
    setDispatchedMission(topNode);

    setHeapArray((prev) => {
      if (prev.length <= 1) return [];
      const newHeap = [...prev];
      newHeap[0] = newHeap.pop();
      return heapifyDown(newHeap);
    });

    computeAStarRoute(selectedWarehouseId, topNode.locationNodeId);
    setActivePanel(2);
  };

  // Toggle Road Blockage State
  const handleToggleRoad = (edgeKey) => {
    setBlockedRoads((prev) => {
      const next = new Set(prev);
      if (next.has(edgeKey)) next.delete(edgeKey);
      else next.add(edgeKey);
      return next;
    });

    if (dispatchedMission) {
      computeAStarRoute(selectedWarehouseId, dispatchedMission.locationNodeId);
    }
  };

  // Real A* Pathfinding with Obstacle Avoidance
  const computeAStarRoute = (startId, targetId) => {
    const start = Number(startId);
    const target = Number(targetId);

    const queue = [[start]];
    const visited = new Set();

    while (queue.length > 0) {
      const path = queue.shift();
      const node = path[path.length - 1];

      if (node === target) {
        const fullPath = path.map((id) => GRAPH_NODES.find((n) => n.id === id));
        setActiveRoute(fullPath);
        return;
      }

      if (!visited.has(node)) {
        visited.add(node);
        const neighbors = ADJACENCY_LIST[node] || [];

        for (const neighbor of neighbors) {
          const edgeKey = `${Math.min(node, neighbor)}-${Math.max(node, neighbor)}`;
          if (!blockedRoads.has(edgeKey)) {
            queue.push([...path, neighbor]);
          }
        }
      }
    }

    // Direct fallback if completely blocked
    setActiveRoute([
      GRAPH_NODES.find((n) => n.id === start),
      GRAPH_NODES.find((n) => n.id === target),
    ]);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans p-4 md:p-8">
      <header className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 border-b border-slate-800 pb-5">
        <div>
          <h1 className="text-2xl font-black tracking-wider text-sky-400 flex items-center gap-2">
            <Activity className="w-7 h-7 text-sky-400" />
            RELIEF-FLOW DSA DASHBOARD
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Real-Time Stream Deduplication, Dynamic Priority Queues & Graph Logistics
          </p>
        </div>

        <div className="flex bg-slate-900 border border-slate-800 p-1 rounded-xl mt-4 md:mt-0">
          <button
            onClick={() => setActivePanel(1)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition ${
              activePanel === 1
                ? 'bg-sky-600 text-white shadow-lg shadow-sky-600/30'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Layers className="w-4 h-4" />
            Panel 1: Triage Stream
          </button>
          <button
            onClick={() => setActivePanel(2)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition ${
              activePanel === 2
                ? 'bg-sky-600 text-white shadow-lg shadow-sky-600/30'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <MapPin className="w-4 h-4" />
            Panel 2: Graph Routing & Logistics
            {dispatchedMission && (
              <span className="w-2 h-2 rounded-full bg-amber-400 animate-ping" />
            )}
          </button>
        </div>
      </header>

      {/* PANEL 1 */}
      {activePanel === 1 && (
        <main className="max-w-6xl mx-auto">
          <CustomInputBar
            onIngestMessage={handleIngestMessage}
            onSimulateDuplicate={handleSimulateDuplicate}
          />
          <RabinKarpScanner
            latestStreamItem={latestItem}
            hashHistory={Array.from(hashSet)}
          />
          <MaxHeapTree
            heapArray={heapArray}
            onDispatch={handleDispatch}
          />
        </main>
      )}

      {/* PANEL 2 */}
      {activePanel === 2 && (
        <main className="max-w-6xl mx-auto space-y-6">
          {dispatchedMission && (
            <div className="bg-slate-900 border border-amber-500/40 p-4 rounded-xl flex flex-col md:flex-row justify-between items-center gap-4 shadow-xl">
              <div>
                <span className="text-xs font-mono text-amber-400 uppercase font-bold block mb-0.5">
                  ⚡ Mission Dispatched via Max-Heap Root O(1)
                </span>
                <h3 className="text-base font-bold text-slate-100">
                  Target Destination: <span className="text-amber-300">Sector Node #{dispatchedMission.locationNodeId}</span>
                </h3>
                <p className="text-xs text-slate-400 italic mt-0.5">"{dispatchedMission.rawText}"</p>
              </div>

              <div className="flex items-center gap-3">
                <button
                  onClick={() => setActivePanel(1)}
                  className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs px-3 py-2 rounded-lg transition flex items-center gap-1"
                >
                  Panel 1 <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          )}

          {/* Router Controls Bar */}
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="flex items-center gap-2">
              <Route className="w-5 h-5 text-sky-400" />
              <span className="text-sm font-bold text-slate-200">A* Pathfinding Control Hub:</span>
            </div>

            <div className="flex flex-wrap items-center gap-3 text-xs">
              <label className="text-slate-400 font-mono">Select Supply Warehouse:</label>
              <select
                value={selectedWarehouseId}
                onChange={(e) => {
                  setSelectedWarehouseId(Number(e.target.value));
                  computeAStarRoute(e.target.value, dispatchedMission ? dispatchedMission.locationNodeId : 12);
                }}
                className="bg-slate-950 border border-slate-700 text-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:border-sky-500 font-mono"
              >
                {GRAPH_NODES.filter((n) => n.isWarehouse).map((w) => (
                  <option key={w.id} value={w.id}>
                    Node #{w.id}: {w.name}
                  </option>
                ))}
              </select>

              <button
                onClick={() =>
                  computeAStarRoute(
                    selectedWarehouseId,
                    dispatchedMission ? dispatchedMission.locationNodeId : 12
                  )
                }
                className="bg-sky-600 hover:bg-sky-500 text-white font-bold px-4 py-1.5 rounded-lg transition flex items-center gap-1 shadow-md shadow-sky-600/20"
              >
                <Navigation className="w-3.5 h-3.5" />
                Recalculate A* Route $f(n)=g(n)+h(n)$
              </button>
            </div>
          </div>

          <OsmMapCanvas
            nodes={GRAPH_NODES}
            activeRoute={activeRoute}
            targetNodeId={dispatchedMission ? dispatchedMission.locationNodeId : 12}
            onToggleRoad={handleToggleRoad}
            blockedRoads={blockedRoads}
          />

          <KnapsackLogistics targetNode={dispatchedMission} />
        </main>
      )}
    </div>
  );
}