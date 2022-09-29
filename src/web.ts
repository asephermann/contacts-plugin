import { WebPlugin } from '@capacitor/core';

import type { ContactsPlugin } from './definitions';

export class ContactsWeb extends WebPlugin implements ContactsPlugin {
  async getContacts(filter: string): Promise<{ results: any[] }> {
    console.log('filter: ', filter);
    return {
      results: [{
        firstName: 'Dummy',
        lastName: 'Entry',
        telephone: '123456'
      }]
    };
  }
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
