import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class TipsService {
  getTips() {
    return [
      {
        id: 1,
        question: 'How to troubleshoot common ATM errors?',
        answer:
          'Start by checking the error code displayed, ensure all cables are properly connected, and verify that the machine has sufficient cash and receipt paper.',
        votes: 0,
        isOpen: false,
      },
      {
        id: 2,
        question: 'Best practices for securing ATMs',
        answer:
          'Implement strong physical security measures, use encrypted communications, regularly update software, and conduct frequent security audits.',
        votes: 0,
        isOpen: false,
      },
      {
        id: 3,
        question: 'Tips for optimizing uptime',
        answer:
          'Perform regular maintenance, monitor cash levels closely, ensure stable network connections, and have a quick response plan for common issues.',
        votes: 0,
        isOpen: false,
      },
    ];
  }

  getSolutions() {
    return [
      {
        id: 1,
        title: 'Communication Failure',
        steps: [
          'Check all network cables for secure connections',
          'Restart the modem or router',
          "Verify the ATM's network settings",
          'Contact your network service provider if the issue persists',
        ],
        icon: 'fas fa-network-wired',
        votes: 0,
        isOpen: false,
      },
      {
        id: 2,
        title: 'Low Cash Levels',
        steps: [
          "Access the ATM's cash management system",
          'Check the current cash levels in each cassette',
          'Replenish cash in low or empty cassettes',
          'Update the cash count in the system',
          'Perform a test transaction to ensure proper dispensing',
        ],
        icon: 'fas fa-money-bill-wave',
        votes: 0,
        isOpen: false,
      },
      {
        id: 3,
        title: 'Power Issues',
        steps: [
          'Verify that the ATM is properly plugged into a power source',
          'Check the circuit breaker or fuse box',
          "Inspect the ATM's internal power supply",
          'Test the backup battery system',
          'If problems persist, contact a certified technician',
        ],
        icon: 'fas fa-bolt',
        votes: 0,
        isOpen: false,
      },
    ];
  }

  getFAQs() {
    return [
      {
        id: 1,
        question: 'How often should ATMs be serviced?',
        answer:
          'ATMs should be serviced regularly, typically every 30-90 days depending on usage. High-traffic ATMs may require more frequent servicing.',
        votes: 0,
        isOpen: false,
      },
      {
        id: 2,
        question: 'What security measures are in place to protect ATMs?',
        answer:
          'ATMs are protected by various security measures including physical locks, alarm systems, surveillance cameras, encrypted communications, and anti-skimming devices.',
        votes: 0,
        isOpen: false,
      },
      {
        id: 3,
        question: 'How are cash levels monitored in ATMs?',
        answer:
          "Cash levels are monitored through the ATM's cash management system, which tracks transactions and provides real-time updates on the amount of cash remaining in each cassette.",
        votes: 0,
        isOpen: false,
      },
      {
        id: 4,
        question: 'What should I do if an ATM is not working?',
        answer:
          'If an ATM is not working, first check for any visible error messages or signs of damage. Then, report the issue to the ATM owner or the support number provided on the machine.',
        votes: 0,
        isOpen: false,
      },
      {
        id: 5,
        question: 'How are software updates handled for ATMs?',
        answer:
          'Software updates for ATMs are typically handled remotely through secure network connections. Updates are usually scheduled during off-peak hours to minimize disruption to service.',
        votes: 0,
        isOpen: false,
      },
    ];
  }
}
