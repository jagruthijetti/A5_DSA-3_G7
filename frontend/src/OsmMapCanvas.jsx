import React from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { Map, Network, Navigation } from 'lucide-react';

// Marker Icons for Leaflet Real Map
const warehouseIcon = new L.Icon({
  iconUrl: 'data:image/svg+xml;base64,' + btoa(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#38bdf8" width="32" height="32">
      <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
    </svg>
  `),
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

const victimIcon = new L.Icon({
  iconUrl: 'data:image/svg+xml;base64,' + btoa(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#f43f5e" width="32" height="32">
      <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
    </svg>
  `),
  iconSize: [32, 32],
  iconAnchor: [16, 32],
});

// Pre-defined road network edges
const EDGES = [
  { from: 1, to: 3 }, { from: 1, to: 4 }, { from: 1, to: 11 },
  { from: 2, to: 6 }, { from: 2, to: 10 }, { from: 2, to: 7 },
  { from: 13, to: 5 }, { from: 13, to: 9 }, { from: 13, to: 4 },
  { from: 14, to: 8 }, { from: 14, to: 3 },
  { from: 15, to: 7 }, { from: 15, to: 11 },
  { from: 4, to: 12 }, { from: 5, to: 12 }, { from: 11, to: 12 },
  { from: 3, to: 8 }, { from: 6, to: 10 }, { from: 7, to: 5 }
];

export default function OsmMapCanvas({ nodes = [], activeRoute = [], targetNodeId, onToggleRoad, blockedRoads = new Set() }) {
  const defaultCenter = [17.4150, 78.4450];

  // Helper to normalize coordinates for SVG canvas
  const getSvgCoords = (node) => {
    const x = ((node.lng - 78.3400) / (78.5600 - 78.3400)) * 720 + 40;
    const y = 390 - ((node.lat - 17.3400) / (17.4900 - 17.3400)) * 340;
    return { x, y };
  };

  const isEdgeInRoute = (fromId, toId) => {
    for (let i = 0; i < activeRoute.length - 1; i++) {
      const u = activeRoute[i].id;
      const v = activeRoute[i + 1].id;
      if ((u === fromId && v === toId) || (u === toId && v === fromId)) return true;
    }
    return false;
  };

  return (
    <div className="space-y-6">
      {/* Legend & Instructions */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-3 flex flex-col md:flex-row justify-between items-center gap-3 text-xs">
        <div className="flex items-center gap-2 text-slate-300">
          <Navigation className="w-4 h-4 text-sky-400" />
          <span>Compare <strong>Real Geographic OpenStreetMap View</strong> above and the <strong>Road Blockage Simulator Topology</strong> below.</span>
        </div>

        <div className="flex items-center gap-4 font-mono">
          <div className="flex items-center gap-1.5 text-sky-400">
            <span className="w-3 h-3 rounded bg-sky-400"></span>
            <span>5 Supply Depots</span>
          </div>
          <div className="flex items-center gap-1.5 text-rose-400">
            <span className="w-3 h-3 rounded-full bg-rose-500"></span>
            <span>Target Sector</span>
          </div>
          <div className="flex items-center gap-1.5 text-rose-500 font-bold">
            <span className="w-4 h-0.5 border-t-2 border-dashed border-rose-500"></span>
            <span>Blocked Road</span>
          </div>
        </div>
      </div>

      {/* Stacked Vertical Layout (Up and Down) */}
      <div className="flex flex-col gap-6">
        
        {/* DISPLAY 1 (TOP): Real OpenStreetMap View */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-xl flex flex-col">
          <div className="flex items-center gap-2 mb-3">
            <Map className="w-5 h-5 text-sky-400" />
            <h3 className="text-sm font-bold text-slate-100">Display 1: Real Geographical Map (OpenStreetMap)</h3>
          </div>

          <div className="h-[420px] w-full rounded-lg overflow-hidden border border-slate-800">
            <MapContainer center={defaultCenter} zoom={12} scrollWheelZoom={true} className="w-full h-full z-0">
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {/* Real Map Markers */}
              {nodes.map((node) => {
                const isTarget = node.id === targetNodeId;
                const isWarehouse = node.isWarehouse;
                return (
                  <Marker
                    key={node.id}
                    position={[node.lat, node.lng]}
                    icon={isWarehouse ? warehouseIcon : victimIcon}
                  >
                    <Popup>
                      <div className="font-sans text-slate-900">
                        <h4 className="font-bold text-sm">{isWarehouse ? node.name : `Sector Node #${node.id}`}</h4>
                        <p className="text-xs text-slate-600">Location: {node.name}</p>
                      </div>
                    </Popup>
                  </Marker>
                );
              })}

              {/* Real Map Active Route Line */}
              {activeRoute && activeRoute.length > 0 && (
                <Polyline
                  positions={activeRoute.map((n) => [n.lat, n.lng])}
                  color="#0284c7"
                  weight={5}
                  opacity={0.8}
                  dashArray="8, 6"
                />
              )}
            </MapContainer>
          </div>
        </div>

        {/* DISPLAY 2 (BOTTOM): Road Network Blockage & Dynamic Topology View */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-xl flex flex-col">
          <div className="flex justify-between items-center mb-3">
            <div className="flex items-center gap-2">
              <Network className="w-5 h-5 text-amber-400" />
              <h3 className="text-sm font-bold text-slate-100">Display 2: Road Blockage & Graph Topology Canvas</h3>
            </div>
            <span className="text-[10px] text-amber-400 font-mono bg-amber-950/60 border border-amber-800 px-2 py-0.5 rounded">
              Click Any Road Line Below to Block / Unblock Route
            </span>
          </div>

          <div className="h-[420px] w-full bg-slate-950 rounded-lg overflow-hidden border border-slate-800 relative">
            <svg className="w-full h-full">
              {/* SVG Edges */}
              {EDGES.map((edge, idx) => {
                const nodeA = nodes.find((n) => n.id === edge.from);
                const nodeB = nodes.find((n) => n.id === edge.to);
                if (!nodeA || !nodeB) return null;

                const posA = getSvgCoords(nodeA);
                const posB = getSvgCoords(nodeB);

                const edgeKey = `${Math.min(edge.from, edge.to)}-${Math.max(edge.from, edge.to)}`;
                const isBlocked = blockedRoads.has(edgeKey);
                const inRoute = isEdgeInRoute(edge.from, edge.to);

                return (
                  <g key={idx} className="cursor-pointer" onClick={() => onToggleRoad(edgeKey)}>
                    <line
                      x1={posA.x} y1={posA.y}
                      x2={posB.x} y2={posB.y}
                      stroke="transparent"
                      strokeWidth="16"
                    />
                    <line
                      x1={posA.x} y1={posA.y}
                      x2={posB.x} y2={posB.y}
                      stroke={isBlocked ? '#f43f5e' : inRoute ? '#38bdf8' : '#334155'}
                      strokeWidth={inRoute ? '5' : isBlocked ? '3' : '2'}
                      strokeDasharray={isBlocked ? '6,6' : inRoute ? '8,4' : 'none'}
                      className="transition-all"
                    />
                  </g>
                );
              })}

              {/* SVG Nodes */}
              {nodes.map((node) => {
                const { x, y } = getSvgCoords(node);
                const isTarget = node.id === targetNodeId;
                const isWarehouse = node.isWarehouse;

                return (
                  <g key={node.id}>
                    <circle
                      cx={x} cy={y}
                      r={isTarget ? 15 : isWarehouse ? 11 : 8}
                      fill={isWarehouse ? '#0284c7' : isTarget ? '#e11d48' : '#1e293b'}
                      stroke={isTarget ? '#fda4af' : isWarehouse ? '#38bdf8' : '#64748b'}
                      strokeWidth={isTarget ? '3' : '2'}
                      className={isTarget ? 'animate-pulse' : ''}
                    />
                    <text
                      x={x} y={y + 22}
                      textAnchor="middle"
                      fill={isTarget ? '#f43f5e' : isWarehouse ? '#38bdf8' : '#94a3b8'}
                      className="text-[10px] font-mono font-bold pointer-events-none select-none"
                    >
                      {isWarehouse ? `[Depot ${node.id}] ${node.name}` : `#${node.id}`}
                    </text>
                  </g>
                );
              })}
            </svg>
          </div>
        </div>

      </div>
    </div>
  );
}