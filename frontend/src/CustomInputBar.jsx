import React, { useState } from 'react';
import { Send, RefreshCw, AlertCircle, MapPin } from 'lucide-react';

export default function CustomInputBar({ onIngestMessage, onSimulateDuplicate }) {
  const [message, setMessage] = useState('');
  const [nodeId, setNodeId] = useState('12');

  // 10 Disaster Sector Nodes
  const disasterNodes = [
    { id: '1', label: 'Node #1 (Sector 1 - Central Hospital Zone)' },
    { id: '2', label: 'Node #2 (Sector 1 - East Residential Complex)' },
    { id: '5', label: 'Node #5 (Sector 2 - Warehouse West Depot)' },
    { id: '8', label: 'Node #8 (Sector 2 - Downtown Metro Square)' },
    { id: '12', label: 'Node #12 (Sector 4 - North River Bridge)' },
    { id: '15', label: 'Node #15 (Sector 4 - South Embankment)' },
    { id: '18', label: 'Node #18 (Sector 5 - Industrial Park East)' },
    { id: '22', label: 'Node #22 (Sector 6 - Highway Relief Shelter)' },
    { id: '27', label: 'Node #27 (Sector 7 - Coastline Fishery Dock)' },
    { id: '30', label: 'Node #30 (Sector 8 - Mountain Foothills Access)' },
  ];

  const sampleScenarios = [
    { label: 'Flash Flood', text: 'Flash flood near Sector 4 bridge, 4 people trapped on roof!', node: '12' },
    { label: 'Building Collapse', text: 'Building collapse on Main St, severe injuries reported!', node: '8' },
    { label: 'Medical Emergency', text: 'Oxygen supplies exhausted at Sector 1 clinic!', node: '1' },
    { label: 'Supply Deficit', text: 'Clean drinking water urgently needed at Sector 6 shelter.', node: '22' },
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!message.trim()) return;
    
    onIngestMessage({
      rawText: message,
      locationNodeId: parseInt(nodeId, 10),
    });
    setMessage('');
  };

  const handleSelectSample = (sample) => {
    setMessage(sample.text);
    setNodeId(sample.node);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-2xl mb-6">
      <div className="flex justify-between items-center mb-3">
        <h2 className="text-xl font-bold text-sky-400 flex items-center gap-2">
          <AlertCircle className="w-5 h-5 text-sky-400" />
          Emergency Command Hub — Custom Message Ingestion
        </h2>
        <span className="text-xs font-mono bg-slate-800 text-slate-400 px-3 py-1 rounded-full border border-slate-700">
          Backend Scoring Engine Connected
        </span>
      </div>

      <div className="flex flex-wrap gap-2 mb-4 items-center">
        <span className="text-xs text-slate-400">Quick Scenarios:</span>
        {sampleScenarios.map((s, idx) => (
          <button
            key={idx}
            type="button"
            onClick={() => handleSelectSample(s)}
            className="text-xs bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white px-2.5 py-1 rounded transition border border-slate-700/50"
          >
            {s.label}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col md:flex-row gap-3">
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Type emergency distress call here..."
          className="flex-1 bg-slate-950 border border-slate-800 text-slate-100 placeholder-slate-500 rounded-lg px-4 py-3 focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 text-sm transition"
        />

        <div className="flex items-center gap-1 bg-slate-950 border border-slate-800 rounded-lg px-2">
          <MapPin className="w-4 h-4 text-sky-400 flex-shrink-0" />
          <select
            value={nodeId}
            onChange={(e) => setNodeId(e.target.value)}
            className="bg-transparent text-slate-200 rounded-lg py-3 text-sm focus:outline-none max-w-[220px]"
          >
            {disasterNodes.map((n) => (
              <option key={n.id} value={n.id} className="bg-slate-900 text-slate-100">
                {n.label}
              </option>
            ))}
          </select>
        </div>

        <button
          type="submit"
          className="bg-sky-600 hover:bg-sky-500 text-white font-semibold px-6 py-3 rounded-lg flex items-center justify-center gap-2 transition shadow-lg shadow-sky-600/20 text-sm"
        >
          <Send className="w-4 h-4" />
          Ingest into Pipeline
        </button>

        <button
          type="button"
          onClick={() => onSimulateDuplicate(message)}
          disabled={!message.trim()}
          className="bg-amber-600/20 hover:bg-amber-600/30 text-amber-400 border border-amber-500/30 font-medium px-4 py-3 rounded-lg flex items-center justify-center gap-2 transition text-sm disabled:opacity-40 disabled:cursor-not-allowed"
        >
          <RefreshCw className="w-4 h-4" />
          Test Duplicate (Rabin-Karp)
        </button>
      </form>
    </div>
  );
}