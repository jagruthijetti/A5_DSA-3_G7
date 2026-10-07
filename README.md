Relief-Flow: Real-Time Stream Deduplication, Dynamic Priority Queues and Graph Logistics Optimization

Problem Statement
During natural disasters and large-scale emergency situations, disaster management and emergency response centers are inundated with thousands of incoming distress signals across multiple digital channels. Existing response systems face two primary operational bottlenecks:

High Volume of Redundant Data: Up to 70% of incoming distress calls are duplicate reports of the same localized event. Processing these redundant signals manually or via linear search routines introduces severe processing latency, delaying life-saving interventions.
Inefficient Logistics and Pathing: Damaged, flooded, or blocked road networks prevent responders from using standard static routes. Additionally, distributing relief supplies from multiple warehouses to affected sectors using unoptimized greedy tactics leads to depot depletion, bottlenecked transit routes, and excessive transportation costs.

A unified algorithmic framework is required to filter redundant stream data in real time, prioritize critical emergencies dynamically, re-route responders around active road blockages, and optimize multi-depot resource allocation under vehicle capacity constraints.

What We Implemented

To address the problem statement, we engineered Relief-Flow, a modular decision-support dashboard constructed entirely using fundamental Data Structures and Algorithms (DSA). The implementation consists of two operational panels:

Panel 1 (Stream Triage and Deduplication):
Rabin-Karp Rolling Hash Engine: Computes polynomial hash values for incoming text messages to avoid expensive string comparisons.
Hash Set Lookup Index: Stores message hashes to detect and filter identical distress reports in constant time.
Binary Max-Heap Priority Queue: Houses unique emergency reports organized by urgency score, maintaining instant root access to the highest-priority mission.

Panel 2 (Graph Navigation and Logistics Optimization):
Weighted Adjacency List Graph: Models the target region as a 15-node geographic network consisting of 5 supply depots and 10 disaster sectors.
A* Pathfinding Engine: Computes optimal routes using Euclidean distance heuristics while monitoring an active hash set of blocked roads to perform dynamic rerouting.
Min-Cost Max-Flow (MCMF) Flow Network: Uses the Shortest Path Faster Algorithm (SPFA) on residual capacity graphs to allocate relief supplies from multiple depots to target sectors at minimum transit cost.
0/1 Knapsack Dynamic Programming Module: Calculates maximum relief item utility per transport vehicle based on strict payload weight bounds.

Getting Started

Prerequisites
Node.js (v18.0.0 or higher)
npm or yarn

Installation and Setup

Clone the repository:
git clone https://github.com/your-username/relief-flow.git
cd relief-flow

Install dependencies:
npm install

Run the application:
npm run dev

Open application:
Navigate to http://localhost:5173 in any standard web browser.
